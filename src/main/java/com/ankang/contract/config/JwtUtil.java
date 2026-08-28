package com.ankang.contract.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT Token的生成、解析和验证功能
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final String secret;
    private final Long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param realName 用户真实姓名
     * @param role 用户角色（ADMIN/USER）
     * @return JWT Token字符串
     */
    public String generateToken(String username, Long userId, String realName, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("realName", realName);
        claims.put("role", role);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     * @throws JwtException Token解析异常
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     * @throws JwtException Token解析异常
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户真实姓名
     *
     * @param token JWT Token
     * @return 用户真实姓名
     * @throws JwtException Token解析异常
     */
    public String getRealNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("realName", String.class);
    }

    /**
     * 从Token中获取用户角色
     *
     * @param token JWT Token
     * @return 用户角色（ADMIN/USER），无该声明时返回 USER
     * @throws JwtException Token解析异常
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String role = claims.get("role", String.class);
        return role == null || role.isEmpty() ? "USER" : role;
    }

    /**
     * 从Token中获取过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     * @throws JwtException Token解析异常
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 检查Token是否过期（不抛出异常）
     * 
     * @param token JWT Token
     * @return true表示已过期，false表示未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // Token已过期
            logger.debug("Token is expired: {}", e.getMessage());
            return true;
        } catch (JwtException e) {
            // Token解析失败，视为过期
            logger.warn("Token parsing failed, treating as expired: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 检查Token是否有效（不抛出异常）
     * 
     * @param token JWT Token
     * @return true表示有效，false表示无效
     */
    public Boolean isTokenValid(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token（抛出异常）
     * 
     * @param token JWT Token
     * @param username 用户名
     * @return true表示验证成功
     * @throws JwtException Token验证异常
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 从Token中解析Claims（可能抛出异常）
     * 
     * @param token JWT Token
     * @return Claims对象
     * @throws JwtException Token解析异常
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 安全地从Token中获取Claims（不抛出异常）
     * 
     * @param token JWT Token
     * @return Claims对象，如果解析失败返回null
     */
    public Claims getClaimsFromTokenSafe(String token) {
        try {
            return getClaimsFromToken(token);
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired, returning claims from exception: {}", e.getMessage());
            return e.getClaims();
        } catch (JwtException e) {
            logger.warn("Token parsing failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     * 
     * @param token JWT Token
     * @return 剩余有效时间（毫秒），如果已过期返回0
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            long remaining = expiration.getTime() - now.getTime();
            return remaining > 0 ? remaining : 0;
        } catch (ExpiredJwtException e) {
            logger.debug("Token already expired: {}", e.getMessage());
            return 0L;
        } catch (JwtException e) {
            logger.warn("Token parsing failed: {}", e.getMessage());
            return 0L;
        }
    }
}
