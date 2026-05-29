package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.WasteVehicle;
import com.ankang.contract.service.WasteVehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆管理控制器
 */
@RestController
@RequestMapping("/waste/vehicles")
public class WasteVehicleController {

    private final WasteVehicleService wasteVehicleService;

    public WasteVehicleController(WasteVehicleService wasteVehicleService) {
        this.wasteVehicleService = wasteVehicleService;
    }

    /**
     * 分页查询车辆
     * @param page 页码
     * @param size 每页数量
     * @param plateNumber 车牌号（模糊搜索）
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResponse<WasteVehicle>> getAll(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Integer status) {
        PageResponse<WasteVehicle> pageResponse = wasteVehicleService.findByPage(page, size, plateNumber, status);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查询所有车辆（下拉选择用）
     * @return 车辆列表
     */
    @GetMapping("/all")
    public ApiResponse<List<WasteVehicle>> getAllVehicles() {
        return ApiResponse.success(wasteVehicleService.findAll());
    }

    /**
     * 根据ID查询车辆
     * @param id 车辆ID
     * @return 车辆信息
     */
    @GetMapping("/{id}")
    public ApiResponse<WasteVehicle> getById(@PathVariable Long id) {
        WasteVehicle vehicle = wasteVehicleService.findById(id);
        if (vehicle == null) {
            return ApiResponse.error("车辆不存在");
        }
        return ApiResponse.success(vehicle);
    }

    /**
     * 新增车辆
     * @param vehicle 车辆信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<String> create(@RequestBody WasteVehicle vehicle) {
        int result = wasteVehicleService.create(vehicle);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    /**
     * 更新车辆
     * @param id 车辆ID
     * @param vehicle 车辆信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody WasteVehicle vehicle) {
        vehicle.setId(id);
        int result = wasteVehicleService.update(vehicle);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 停用车辆
     * @param id 车辆ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = wasteVehicleService.delete(id);
        if (result > 0) {
            return ApiResponse.success("停用成功", null);
        }
        return ApiResponse.error("停用失败");
    }

    /**
     * 启用车辆
     * @param id 车辆ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<String> enable(@PathVariable Long id) {
        int result = wasteVehicleService.enable(id);
        if (result > 0) {
            return ApiResponse.success("启用成功", null);
        }
        return ApiResponse.error("启用失败");
    }
}
