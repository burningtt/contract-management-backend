package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.WasteRecord;
import com.ankang.contract.service.WasteRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 医废记录管理控制器
 */
@RestController
@RequestMapping("/waste/records")
public class WasteRecordController {

    private final WasteRecordService wasteRecordService;

    public WasteRecordController(WasteRecordService wasteRecordService) {
        this.wasteRecordService = wasteRecordService;
    }

    /**
     * 分页查询医废记录
     * @param page 页码
     * @param size 每页数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称（模糊搜索）
     * @param batchNo 批次编号
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResponse<WasteRecord>> getAll(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) String batchNo) {
        PageResponse<WasteRecord> pageResponse = wasteRecordService.findByPage(
            page, size, startDate, endDate, region, hospitalName, batchNo);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 记录信息
     */
    @GetMapping("/{id}")
    public ApiResponse<WasteRecord> getById(@PathVariable Long id) {
        WasteRecord record = wasteRecordService.findById(id);
        if (record == null) {
            return ApiResponse.error("记录不存在");
        }
        return ApiResponse.success(record);
    }

    /**
     * 查询指定医院最近一条记录（用于获取上次选择的车辆和人员）
     * @param hospitalNo 医院编号
     * @return 最近一条记录
     */
    @GetMapping("/last/{hospitalNo}")
    public ApiResponse<WasteRecord> getLastRecord(@PathVariable String hospitalNo) {
        WasteRecord record = wasteRecordService.findLastByHospitalNo(hospitalNo);
        return ApiResponse.success(record);
    }

    /**
     * 新增医废记录
     * @param record 记录信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<String> create(@RequestBody WasteRecord record) {
        int result = wasteRecordService.create(record);
        if (result > 0) {
            return ApiResponse.success("录入成功", null);
        }
        return ApiResponse.error("录入失败");
    }

    /**
     * 更新医废记录
     * @param id 记录ID
     * @param record 记录信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody WasteRecord record) {
        record.setId(id);
        int result = wasteRecordService.update(record);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 删除医废记录
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = wasteRecordService.delete(id);
        if (result > 0) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error("删除失败");
    }

    /**
     * 导出医废记录为Excel文件
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称
     * @param batchNo 批次编号
     * @return Excel文件
     * @throws IOException 写入异常
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) String batchNo) throws IOException {
        byte[] data = wasteRecordService.exportToExcel(startDate, endDate, region, hospitalName, batchNo);

        String fileName = URLEncoder.encode("医废记录_" + LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx",
            StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(data);
    }
}
