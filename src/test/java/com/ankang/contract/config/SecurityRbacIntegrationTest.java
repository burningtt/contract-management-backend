package com.ankang.contract.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RBAC安全集成测试（使用local profile连接本地数据库）
 * 验证前端登录流程与后端权限体系的一致性：
 * 1. 未认证请求返回标准化401
 * 2. USER角色访问管理接口返回标准化403
 * 3. ADMIN角色访问管理接口返回200
 */
@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
class SecurityRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 通过真实登录接口获取ADMIN角色的token，同时验证登录链路
     */
    @Test
    @DisplayName("admin登录返回携带ADMIN角色的token")
    void loginReturnsTokenWithAdminRole() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        // 从响应中提取token并验证其中包含ADMIN角色claim
        String token = response.split("\"token\":\"")[1].split("\"")[0];
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token));
    }

    /**
     * 未携带token访问受保护接口应返回标准化401 JSON
     */
    @Test
    @DisplayName("未认证请求返回标准化401错误响应")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("authentication_error"))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    /**
     * 无效token访问受保护接口应返回标准化401 JSON
     */
    @Test
    @DisplayName("无效token返回标准化401错误响应")
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer invalid.token.value"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("authentication_error"));
    }

    /**
     * USER角色访问ADMIN专属接口应返回标准化403 JSON（RBAC核心场景）
     */
    @Test
    @DisplayName("USER角色访问管理接口返回标准化403错误响应")
    void userRoleAccessingAdminEndpointReturns403() throws Exception {
        // 直接使用JwtUtil签发USER角色token（模拟普通用户登录后的凭证）
        String userToken = jwtUtil.generateToken("normaluser", 999L, "普通用户", "USER");

        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("authorization_error"))
            .andExpect(jsonPath("$.message").value("权限不足，禁止访问该资源"));
    }

    /**
     * ADMIN角色访问管理接口应返回200
     */
    @Test
    @DisplayName("ADMIN角色访问管理接口返回200")
    void adminRoleAccessingAdminEndpointReturns200() throws Exception {
        String adminToken = jwtUtil.generateToken("admin", 1L, "系统管理员", "ADMIN");

        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    /**
     * 公开端点无需认证即可访问
     */
    @Test
    @DisplayName("公开端点无需认证可访问")
    void publicEndpointAccessibleWithoutToken() throws Exception {
        assertNotNull(mockMvc);
        // /test/** 为公开端点
        mockMvc.perform(get("/test/encode").param("password", "test123"))
            .andExpect(status().isOk());
    }
}
