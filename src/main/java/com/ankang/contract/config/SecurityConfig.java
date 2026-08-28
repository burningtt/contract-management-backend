package com.ankang.contract.config;

import com.ankang.contract.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置
 * 统一实施JWT认证与基于角色的访问控制（RBAC）
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 安全过滤链配置
     * - 公开端点：登录、注册、测试接口、医废机构导入模板
     * - 用户管理端点：仅ADMIN角色可访问（RBAC）
     * - 其余端点：需认证
     * - 未认证返回401，权限不足返回403，均为标准化JSON响应
     *
     * @param http HttpSecurity配置
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(contentType -> contentType.disable())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/test/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/waste/hospitals/import/template").permitAll()
                // 用户管理接口仅ADMIN角色可访问（RBAC）
                .requestMatchers("/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // 未认证（无token/token无效过期）：返回标准化401 JSON
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "authentication_error", "未登录或登录已失效，请重新登录"))
                // 已认证但权限不足：返回标准化403 JSON
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                        "authorization_error", "权限不足，禁止访问该资源"))
            )
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 写出标准化的错误JSON响应
     *
     * @param response HTTP响应
     * @param status HTTP状态码（401/403）
     * @param error 错误类型标识
     * @param message 错误描述
     * @throws java.io.IOException 写出异常
     */
    private void writeJsonError(HttpServletResponse response, int status, String error, String message)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 构造与ApiResponse风格一致的错误JSON
        String errorResponse = String.format(
            "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"error\":\"%s\"}",
            status, message, error);
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }

    /**
     * 密码编码器（BCrypt）
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
