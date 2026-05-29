package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.dto.WasteHospitalImportDTO;
import com.ankang.contract.dto.WasteHospitalImportResultDTO;
import com.ankang.contract.entity.WasteHospital;
import com.ankang.contract.service.WasteHospitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 医院管理控制器
 */
@RestController
@RequestMapping("/waste/hospitals")
public class WasteHospitalController {

    private static final Logger logger = LoggerFactory.getLogger(WasteHospitalController.class);

    private final WasteHospitalService wasteHospitalService;

    public WasteHospitalController(WasteHospitalService wasteHospitalService) {
        this.wasteHospitalService = wasteHospitalService;
    }

    /**
     * 分页查询医院
     * @param page 页码
     * @param size 每页数量
     * @param hospitalName 医院名称（模糊搜索）
     * @param region 地区编码
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResponse<WasteHospital>> getAll(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer status) {
        PageResponse<WasteHospital> pageResponse = wasteHospitalService.findByPage(page, size, hospitalName, region, status);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查询所有医院（下拉选择用）
     * @return 医院列表
     */
    @GetMapping("/all")
    public ApiResponse<List<WasteHospital>> getAllHospitals() {
        return ApiResponse.success(wasteHospitalService.findAll());
    }

    /**
     * 根据ID查询医院
     * @param id 医院ID
     * @return 医院信息
     */
    @GetMapping("/{id}")
    public ApiResponse<WasteHospital> getById(@PathVariable Long id) {
        WasteHospital hospital = wasteHospitalService.findById(id);
        if (hospital == null) {
            return ApiResponse.error("医院不存在");
        }
        return ApiResponse.success(hospital);
    }

    /**
     * 新增医院
     * @param hospital 医院信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<String> create(@RequestBody WasteHospital hospital) {
        int result = wasteHospitalService.create(hospital);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    /**
     * 更新医院
     * @param id 医院ID
     * @param hospital 医院信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody WasteHospital hospital) {
        hospital.setId(id);
        int result = wasteHospitalService.update(hospital);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 停用医院
     * @param id 医院ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = wasteHospitalService.delete(id);
        if (result > 0) {
            return ApiResponse.success("停用成功", null);
        }
        return ApiResponse.error("停用失败");
    }

    /**
     * 启用医院
     * @param id 医院ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<String> enable(@PathVariable Long id) {
        int result = wasteHospitalService.enable(id);
        if (result > 0) {
            return ApiResponse.success("启用成功", null);
        }
        return ApiResponse.error("启用失败");
    }

    /**
     * 解析并验证Excel文件
     * @param file 上传的Excel文件
     * @return 解析结果
     */
    @PostMapping("/import/parse")
    public ResponseEntity<ApiResponse<WasteHospitalImportResultDTO>> parseExcel(
            @RequestParam("file") MultipartFile file) {
        try {
            logger.info("开始解析导入文件: {}", file.getOriginalFilename());
            WasteHospitalImportResultDTO result = wasteHospitalService.parseAndValidate(file);
            logger.info("文件解析完成: 总数={}, 有效={}, 无效={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalArgumentException e) {
            logger.warn("文件验证失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            logger.error("解析文件失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("解析文件失败: " + e.getMessage()));
        }
    }

    /**
     * 执行批量导入
     * @param dataList 待导入的数据列表
     * @return 导入结果
     */
    @PostMapping("/import/execute")
    public ResponseEntity<ApiResponse<WasteHospitalImportResultDTO>> executeImport(
            @RequestBody List<WasteHospitalImportDTO> dataList) {
        try {
            logger.info("开始执行导入，数据量: {}", dataList.size());
            WasteHospitalImportResultDTO result = wasteHospitalService.importData(dataList);
            logger.info("导入完成: 成功={}, 失败={}", result.getSuccessCount(), result.getFailCount());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("导入数据失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("导入数据失败: " + e.getMessage()));
        }
    }

    /**
     * 下载导入模板
     * @return 模板文件
     */
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = wasteHospitalService.generateTemplate();
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=hospital_import_template.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(template);
        } catch (Exception e) {
            logger.error("生成模板失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
