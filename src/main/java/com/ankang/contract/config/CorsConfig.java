package com.ankang.contract.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    /**
     * 配置CORS（跨域资源共享）
     * 允许来自指定域名的跨域请求
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许携带凭证（cookies等）
        config.setAllowCredentials(true);
        
        // 允许的域名（生产环境）
        config.addAllowedOrigin("http://43.142.255.169:99");
        config.addAllowedOrigin("http://43.142.255.169");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:4173");
        config.addAllowedOrigin("http://localhost:99");
        config.addAllowedOriginPattern("*"); // 允许所有域名（开发环境）
        
        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList("*"));
        
        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
