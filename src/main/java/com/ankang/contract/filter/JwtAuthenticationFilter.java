package com.ankang.contract.filter;

import com.ankang.contract.config.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * JWT认证过滤器
 * 处理JWT Token验证和用户认证
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String requestURI = request.getRequestURI();

        // 从Authorization头获取token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 支持通过URL参数传递token，用于文件预览等场景
        if (token == null) {
            token = request.getParameter("token");
        }

        // 如果token存在，进行验证
        if (token != null) {
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 验证token有效性
                    if (jwtUtil.validateToken(token, username)) {
                        String realName = jwtUtil.getRealNameFromToken(token);
                        // 从token中解析角色并授予对应的GrantedAuthority，供RBAC鉴权使用
                        String role = jwtUtil.getRoleFromToken(token);
                        List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                        );
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                        authentication.setDetails(realName);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        logger.debug("JWT token validated successfully for user: {} with role: {} on URI: {}",
                            username, role, requestURI);
                    } else {
                        logger.warn("JWT token validation failed for user: {} on URI: {}", username, requestURI);
                        handleTokenError(response, "Token验证失败", HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
            } catch (ExpiredJwtException e) {
                // Token已过期
                logger.warn("JWT token expired for URI: {}. Expired at: {}. Error: {}", 
                    requestURI, e.getClaims().getExpiration(), e.getMessage());
                
                // 清除安全上下文
                SecurityContextHolder.clearContext();
                
                // 返回401错误和明确的错误信息
                handleTokenError(response, "Token已过期，请重新登录", HttpServletResponse.SC_UNAUTHORIZED);
                return;
                
            } catch (JwtException e) {
                // JWT相关异常（签名错误、格式错误等）
                logger.error("JWT token parsing error for URI: {}. Error: {}", requestURI, e.getMessage());
                
                // 清除安全上下文
                SecurityContextHolder.clearContext();
                
                // 返回401错误
                handleTokenError(response, "Token无效", HttpServletResponse.SC_UNAUTHORIZED);
                return;
                
            } catch (Exception e) {
                // 其他异常
                logger.error("Unexpected error during JWT validation for URI: {}. Error: {}", requestURI, e.getMessage());
                
                // 清除安全上下文
                SecurityContextHolder.clearContext();
                
                // 继续过滤链，让后续处理决定
                filterChain.doFilter(request, response);
                return;
            }
        }
        
        // Token不存在或验证通过，继续过滤链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 处理Token错误，返回错误响应
     * 
     * @param response HTTP响应对象
     * @param message 错误消息
     * @param status HTTP状态码
     * @throws IOException IO异常
     */
    private void handleTokenError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // 构造错误响应JSON
        String errorResponse = String.format("{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}", 
            "authentication_error", message, status);
        
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }
}