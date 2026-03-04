package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.FeeStatisticsDTO;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.Institution;
import com.ankang.contract.service.InstitutionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Institution>> getAll(
        @RequestParam(required = false, defaultValue = "1") int page,
        @RequestParam(required = false, defaultValue = "10") int size,
        @RequestParam(required = false) String institutionName,
        @RequestParam(required = false) String institutionType,
        @RequestParam(required = false) String institutionStatus,
        @RequestParam(required = false) String region) {
        PageResponse<Institution> pageResponse = institutionService.findByPage(page, size, institutionName, institutionType, institutionStatus, region);
        return ApiResponse.success(pageResponse);
    }

    @GetMapping("/fee-statistics")
    public ApiResponse<PageResponse<FeeStatisticsDTO>> getFeeStatistics(
        @RequestParam(required = false, defaultValue = "1") int page,
        @RequestParam(required = false, defaultValue = "10") int size,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String institutionName) {
        PageResponse<FeeStatisticsDTO> pageResponse = institutionService.findFeeStatistics(region, year, institutionName, page, size);
        return ApiResponse.success(pageResponse);
    }

    @GetMapping("/{id}")
    public ApiResponse<Institution> getById(@PathVariable Long id) {
        Institution institution = institutionService.findById(id);
        if (institution == null) {
            return ApiResponse.error("机构不存在");
        }
        return ApiResponse.success(institution);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<Institution>> getByType(@PathVariable String type) {
        return ApiResponse.success(institutionService.findByType(type));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Institution>> getByStatus(@PathVariable String status) {
        return ApiResponse.success(institutionService.findByStatus(status));
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody Institution institution) {
        int result = institutionService.create(institution);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Institution institution) {
        institution.setId(id);
        int result = institutionService.update(institution);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = institutionService.delete(id);
        if (result > 0) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error("删除失败");
    }
}
