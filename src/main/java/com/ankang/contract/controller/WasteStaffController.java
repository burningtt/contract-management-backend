package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.WasteStaff;
import com.ankang.contract.service.WasteStaffService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车队人员管理控制器
 */
@RestController
@RequestMapping("/waste/staff")
public class WasteStaffController {

    private final WasteStaffService wasteStaffService;

    public WasteStaffController(WasteStaffService wasteStaffService) {
        this.wasteStaffService = wasteStaffService;
    }

    /**
     * 分页查询人员
     * @param page 页码
     * @param size 每页数量
     * @param staffName 姓名（模糊搜索）
     * @param position 岗位
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResponse<WasteStaff>> getAll(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String staffName,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer status) {
        PageResponse<WasteStaff> pageResponse = wasteStaffService.findByPage(page, size, staffName, position, status);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查询所有人员（下拉选择用）
     * @return 人员列表
     */
    @GetMapping("/all")
    public ApiResponse<List<WasteStaff>> getAllStaff() {
        return ApiResponse.success(wasteStaffService.findAll());
    }

    /**
     * 根据ID查询人员
     * @param id 人员ID
     * @return 人员信息
     */
    @GetMapping("/{id}")
    public ApiResponse<WasteStaff> getById(@PathVariable Long id) {
        WasteStaff staff = wasteStaffService.findById(id);
        if (staff == null) {
            return ApiResponse.error("人员不存在");
        }
        return ApiResponse.success(staff);
    }

    /**
     * 新增人员
     * @param staff 人员信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<String> create(@RequestBody WasteStaff staff) {
        int result = wasteStaffService.create(staff);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    /**
     * 更新人员
     * @param id 人员ID
     * @param staff 人员信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody WasteStaff staff) {
        staff.setId(id);
        int result = wasteStaffService.update(staff);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 停用人员
     * @param id 人员ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = wasteStaffService.delete(id);
        if (result > 0) {
            return ApiResponse.success("停用成功", null);
        }
        return ApiResponse.error("停用失败");
    }

    /**
     * 启用人员
     * @param id 人员ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<String> enable(@PathVariable Long id) {
        int result = wasteStaffService.enable(id);
        if (result > 0) {
            return ApiResponse.success("启用成功", null);
        }
        return ApiResponse.error("启用失败");
    }
}
