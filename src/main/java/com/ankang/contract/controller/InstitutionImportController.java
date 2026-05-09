package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.InstitutionImportDTO;
import com.ankang.contract.dto.InstitutionImportResultDTO;
import com.ankang.contract.service.InstitutionImportService;
import com.ankang.contract.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 机构导入控制器
 */
@RestController
@RequestMapping("/institutions/import")
public class InstitutionImportController {

    private static final Logger logger = LoggerFactory.getLogger(InstitutionImportController.class);

    private final InstitutionImportService importService;
    private final JwtUtil jwtUtil;

    public InstitutionImportController(InstitutionImportService importService, JwtUtil jwtUtil) {
        this.importService = importService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 解析并验证Excel文件
     * @param file 上传的Excel文件
     * @return 解析结果
     */
    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<InstitutionImportResultDTO>> parseExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            String operator = getOperatorFromRequest(request);
            logger.info("用户 {} 开始解析导入文件: {}", operator, file.getOriginalFilename());

            InstitutionImportResultDTO result = importService.parseAndValidate(file);

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
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<InstitutionImportResultDTO>> executeImport(
            @RequestBody List<InstitutionImportDTO> dataList,
            HttpServletRequest request) {
        try {
            String operator = getOperatorFromRequest(request);
            logger.info("用户 {} 开始执行导入，数据量: {}", operator, dataList.size());

            InstitutionImportResultDTO result = importService.importData(dataList, operator);

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
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = generateTemplate();
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=institution_import_template.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(template);
        } catch (Exception e) {
            logger.error("生成模板失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getOperatorFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                logger.warn("解析token失败", e);
            }
        }
        return "unknown";
    }

    private byte[] generateTemplate() throws IOException {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("机构导入模板");

            String[] headers = {"地区", "机构名称", "联系电话", "机构类型", "联系人", "实际床位", "开放床位", "备注"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);

                sheet.setColumnWidth(i, 20 * 256);
            }

            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("汉滨区");
            exampleRow.createCell(1).setCellValue("示例医疗机构");
            exampleRow.createCell(2).setCellValue("0915-1234567");
            exampleRow.createCell(3).setCellValue("综合医院");
            exampleRow.createCell(4).setCellValue("张三");
            exampleRow.createCell(5).setCellValue("100");
            exampleRow.createCell(6).setCellValue("80");
            exampleRow.createCell(7).setCellValue("备注信息");

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}
