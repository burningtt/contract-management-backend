package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.ChangePasswordRequest;
import com.ankang.contract.entity.SysUser;
import com.ankang.contract.mapper.SysUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
public class SysUserController {

    private final SysUserMapper sysUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public SysUserController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 查询所有用户
     * @return 用户列表
     */
    @GetMapping
    public ApiResponse<List<SysUser>> getAllUsers() {
        List<SysUser> users = sysUserMapper.findAll();
        return ApiResponse.success(users);
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ApiResponse<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserMapper.findById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(user);
    }

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<String> createUser(@RequestBody SysUser user) {
        // 检查用户名是否已存在
        SysUser existingUser = sysUserMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return ApiResponse.error("用户名已存在");
        }
        
        // 对密码进行BCrypt加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);
        }
        
        int result = sysUserMapper.insert(user);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse<String> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        
        // 如果提供了新密码，则进行BCrypt加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);
        }
        
        int result = sysUserMapper.update(user);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        int result = sysUserMapper.deleteById(id);
        if (result > 0) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error("删除失败");
    }

    /**
     * 修改当前用户密码
     * @param request 密码修改请求
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody ChangePasswordRequest request) {
        // 获取当前登录用户信息（从JWT token中解析）
        // 这里简化处理，实际应该从SecurityContext中获取
        String username = getCurrentUsername();
        if (username == null) {
            return ApiResponse.error("未登录");
        }

        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.error("旧密码错误");
        }

        // 加密新密码
        String encryptedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encryptedNewPassword);

        int result = sysUserMapper.update(user);
        if (result > 0) {
            return ApiResponse.success("密码修改成功", null);
        }
        return ApiResponse.error("密码修改失败");
    }

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    private String getCurrentUsername() {
        // 从请求头中获取JWT token并解析用户名
        // 这里简化处理，实际应该使用JwtUtil解析token
        // 由于有JWT过滤器，这里可以通过SecurityContextHolder获取
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}
