package com.ankang.contract.filter;

import com.ankang.contract.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JwtAuthenticationFilter单元测试
 * 验证角色authorities的授予逻辑与标准化401响应
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    private static final String TOKEN = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtil);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 构造携带Authorization头的请求
     *
     * @return Mock请求对象
     */
    private MockHttpServletRequest buildRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        request.setRequestURI("/api/institutions");
        return request;
    }

    /**
     * ADMIN角色token应授予ROLE_ADMIN权限
     */
    @Test
    @DisplayName("ADMIN角色token授予ROLE_ADMIN权限")
    void adminTokenGrantsAdminAuthority() throws Exception {
        MockHttpServletRequest request = buildRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.getUsernameFromToken(TOKEN)).thenReturn("admin");
        when(jwtUtil.validateToken(TOKEN, "admin")).thenReturn(true);
        when(jwtUtil.getRealNameFromToken(TOKEN)).thenReturn("系统管理员");
        when(jwtUtil.getRoleFromToken(TOKEN)).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_ADMIN")));
    }

    /**
     * USER角色token应授予ROLE_USER权限且不含ADMIN权限
     */
    @Test
    @DisplayName("USER角色token授予ROLE_USER权限")
    void userTokenGrantsUserAuthority() throws Exception {
        MockHttpServletRequest request = buildRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.getUsernameFromToken(TOKEN)).thenReturn("zhangsan");
        when(jwtUtil.validateToken(TOKEN, "zhangsan")).thenReturn(true);
        when(jwtUtil.getRealNameFromToken(TOKEN)).thenReturn("张三");
        when(jwtUtil.getRoleFromToken(TOKEN)).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_USER")));
        assertFalse(auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_ADMIN")));
    }

    /**
     * 过期token应返回标准化401 JSON响应并清空安全上下文
     */
    @Test
    @DisplayName("过期token返回标准化401响应")
    void expiredTokenReturnsStandardized401() throws Exception {
        MockHttpServletRequest request = buildRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 构造带过期时间claim的异常，模拟真实过期场景（filter日志会读取getExpiration）
        Claims expiredClaims = Jwts.claims()
            .expiration(new java.util.Date(System.currentTimeMillis() - 1000))
            .build();
        ExpiredJwtException expiredException = new ExpiredJwtException(
            null, expiredClaims, "token expired");
        when(jwtUtil.getUsernameFromToken(TOKEN)).thenThrow(expiredException);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("authentication_error"));
        assertTrue(body.contains("Token已过期"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, org.mockito.Mockito.never()).doFilter(request, response);
    }

    /**
     * 无token的请求应直接放行（由SecurityFilterChain决定是否拒绝）
     */
    @Test
    @DisplayName("无token请求直接放行进入后续过滤链")
    void noTokenContinuesFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
