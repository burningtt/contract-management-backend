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

    /** 各县市区域代码表（代码-名称），与前端 region.js 保持一致 */
    private static final String[][] REGION_CODES = {
        {"XFQ", "忻府区"},
        {"YPS", "原平市"},
        {"DXX", "定襄县"},
        {"WTX", "五台县"},
        {"DZX", "代县"},
        {"FSX", "繁峙县"},
        {"JLX", "静乐县"},
        {"NWX", "宁武县"},
        {"SCX", "神池县"},
        {"WZX", "五寨县"},
        {"KLX", "岢岚县"},
        {"BDX", "保德县"},
        {"HQX", "河曲县"},
        {"PGX", "偏关县"}
    };

    /** 医疗机构类别代码表（代码-名称），与前端机构管理页面保持一致 */
    private static final String[][] INSTITUTION_TYPES = {
        {"HOSPITAL", "医院"},
        {"CLINIC", "诊所"},
        {"COMMUNITY", "社区卫生服务中心"},
        {"PHARMACY", "药店"},
        {"LABORATORY", "检验中心"},
        {"HEALTH_CENTER", "卫生院"},
        {"HEALTH_STATION", "卫生所"},
        {"OTHER", "其他"}
    };

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

    /**
     * 生成导入模板（含代码说明工作表）
     * @return 模板文件字节
     * @throws IOException 生成模板失败时抛出
     */
    private byte[] generateTemplate() throws IOException {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            // 表头加粗+灰色底纹样式
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // ===== Sheet1：机构导入模板 =====
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("机构导入模板");
            String[] headers = {"地区", "机构名称", "联系电话", "机构类型", "联系人", "实际床位", "开放床位", "备注"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // 示例行使用标准代码（地区填区域代码、机构类型填类别代码）
            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("XFQ");
            exampleRow.createCell(1).setCellValue("示例医疗机构（此行数据不会导入，请删除）");
            exampleRow.createCell(2).setCellValue("0915-1234567");
            exampleRow.createCell(3).setCellValue("HOSPITAL");
            exampleRow.createCell(4).setCellValue("张三");
            exampleRow.createCell(5).setCellValue("100");
            exampleRow.createCell(6).setCellValue("80");
            exampleRow.createCell(7).setCellValue("备注信息");

            // ===== Sheet2：代码说明 =====
            org.apache.poi.ss.usermodel.Sheet codeSheet = workbook.createSheet("代码说明");
            int rowIdx = 0;

            // 区域代码表
            org.apache.poi.ss.usermodel.Row regionTitleRow = codeSheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell regionTitleCell = regionTitleRow.createCell(0);
            regionTitleCell.setCellValue("各县市区域代码表（地区列可填代码或名称）");
            regionTitleCell.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Row regionHeaderRow = codeSheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell regionCodeHeader = regionHeaderRow.createCell(0);
            regionCodeHeader.setCellValue("区域代码");
            regionCodeHeader.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell regionNameHeader = regionHeaderRow.createCell(1);
            regionNameHeader.setCellValue("区域名称");
            regionNameHeader.setCellStyle(headerStyle);
            for (String[] region : REGION_CODES) {
                org.apache.poi.ss.usermodel.Row row = codeSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(region[0]);
                row.createCell(1).setCellValue(region[1]);
            }

            // 空行分隔
            rowIdx++;

            // 机构类别代码表
            org.apache.poi.ss.usermodel.Row typeTitleRow = codeSheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell typeTitleCell = typeTitleRow.createCell(0);
            typeTitleCell.setCellValue("医疗机构类别代码表（机构类型列可填代码或名称）");
            typeTitleCell.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Row typeHeaderRow = codeSheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell typeCodeHeader = typeHeaderRow.createCell(0);
            typeCodeHeader.setCellValue("类别代码");
            typeCodeHeader.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell typeNameHeader = typeHeaderRow.createCell(1);
            typeNameHeader.setCellValue("类别名称");
            typeNameHeader.setCellStyle(headerStyle);
            for (String[] type : INSTITUTION_TYPES) {
                org.apache.poi.ss.usermodel.Row row = codeSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(type[0]);
                row.createCell(1).setCellValue(type[1]);
            }

            codeSheet.setColumnWidth(0, 30 * 256);
            codeSheet.setColumnWidth(1, 40 * 256);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}
