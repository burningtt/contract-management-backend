package com.ankang.contract.config;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil单元测试
 * 重点验证角色claim的生成与解析，保证前后端RBAC一致性
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("ankang-contract-management-secret-key-2024", 86400000L);
    }

    /**
     * 验证生成的token包含角色claim且能正确解析
     */
    @Test
    @DisplayName("生成包含ADMIN角色的token并正确解析")
    void generateTokenWithAdminRole() {
        String token = jwtUtil.generateToken("admin", 1L, "系统管理员", "ADMIN");

        assertNotNull(token);
        assertEquals("admin", jwtUtil.getUsernameFromToken(token));
        assertEquals(1L, jwtUtil.getUserIdFromToken(token));
        assertEquals("系统管理员", jwtUtil.getRealNameFromToken(token));
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token));
    }

    /**
     * 验证USER角色的token解析
     */
    @Test
    @DisplayName("生成包含USER角色的token并正确解析")
    void generateTokenWithUserRole() {
        String token = jwtUtil.generateToken("zhangsan", 2L, "张三", "USER");

        assertEquals("USER", jwtUtil.getRoleFromToken(token));
        assertEquals("zhangsan", jwtUtil.getUsernameFromToken(token));
    }

    /**
     * 旧版token（无role claim）应默认解析为USER，保证向下兼容
     */
    @Test
    @DisplayName("无role声明的token默认解析为USER角色")
    void missingRoleClaimDefaultsToUser() {
        // 手工构造不含role claim的token（模拟旧版本签发的token）
        String token = jwtUtil.generateToken("legacy", 3L, "老用户", null);

        assertEquals("USER", jwtUtil.getRoleFromToken(token));
    }

    /**
     * 验证token有效性与过期校验
     */
    @Test
    @DisplayName("有效token校验通过")
    void validateTokenSuccess() {
        String token = jwtUtil.generateToken("admin", 1L, "系统管理员", "ADMIN");

        assertTrue(jwtUtil.validateToken(token, "admin"));
        assertTrue(jwtUtil.isTokenValid(token));
        assertTrue(jwtUtil.getTokenRemainingTime(token) > 0);
    }

    /**
     * 用户名不匹配时token校验失败
     */
    @Test
    @DisplayName("用户名不匹配时token校验失败")
    void validateTokenWrongUsername() {
        String token = jwtUtil.generateToken("admin", 1L, "系统管理员", "ADMIN");

        assertFalse(jwtUtil.validateToken(token, "other"));
    }

    /**
     * 非法格式token解析应抛出JwtException
     */
    @Test
    @DisplayName("非法格式token解析抛出JwtException")
    void parseInvalidTokenThrows() {
        assertThrows(JwtException.class, () -> jwtUtil.getRoleFromToken("invalid-token"));
    }
}
