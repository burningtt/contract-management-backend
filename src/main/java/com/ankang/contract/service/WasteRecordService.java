package com.ankang.contract.service;

import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.WasteRecord;
import com.ankang.contract.exception.BusinessException;
import com.ankang.contract.mapper.WasteRecordMapper;
import com.ankang.contract.util.SecurityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 医废记录管理服务
 */
@Service
public class WasteRecordService {

    private static final Logger logger = LoggerFactory.getLogger(WasteRecordService.class);
    private static final String BATCH_PREFIX = "YF";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final java.util.Map<String, String> REGION_MAP = java.util.Map.ofEntries(
        java.util.Map.entry("XFQ", "忻府区"), java.util.Map.entry("YPS", "原平市"),
        java.util.Map.entry("DXX", "定襄县"), java.util.Map.entry("WTX", "五台县"),
        java.util.Map.entry("DZX", "代县"), java.util.Map.entry("FSX", "繁峙县"),
        java.util.Map.entry("JLX", "静乐县"), java.util.Map.entry("NWX", "宁武县"),
        java.util.Map.entry("SCX", "神池县"), java.util.Map.entry("WZX", "五寨县"),
        java.util.Map.entry("KLX", "岢岚县"), java.util.Map.entry("BDX", "保德县"),
        java.util.Map.entry("HQX", "河曲县"), java.util.Map.entry("PGX", "偏关县")
    );

    private final WasteRecordMapper wasteRecordMapper;

    public WasteRecordService(WasteRecordMapper wasteRecordMapper) {
        this.wasteRecordMapper = wasteRecordMapper;
    }

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 记录信息（含关联名称）
     */
    public WasteRecord findById(Long id) {
        return wasteRecordMapper.findById(id);
    }

    /**
     * 分页查询记录
     * @param page 页码
     * @param size 每页数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称（模糊搜索）
     * @param batchNo 批次编号
     * @return 分页结果
     */
    public PageResponse<WasteRecord> findByPage(int page, int size, LocalDate startDate,
                                                 LocalDate endDate, String region,
                                                 String hospitalName, String batchNo) {
        int offset = (page - 1) * size;
        java.util.List<WasteRecord> rows = wasteRecordMapper.findByPage(
            offset, size, startDate, endDate, region, hospitalName, batchNo);
        Long total = wasteRecordMapper.count(startDate, endDate, region, hospitalName, batchNo);
        return new PageResponse<>(rows, total);
    }

    /**
     * 查询指定医院最近一条记录（用于获取上次选择的车辆和人员）
     * @param hospitalNo 医院编号
     * @return 最近一条记录
     */
    public WasteRecord findLastByHospitalNo(String hospitalNo) {
        return wasteRecordMapper.findLastByHospitalNo(hospitalNo);
    }

    /**
     * 新增记录
     * @param record 记录信息
     * @return 影响行数
     */
    @Transactional
    public int create(WasteRecord record) {
        // 生成批次编号
        String batchNo = generateBatchNo(record.getRecordDate());
        record.setBatchNo(batchNo);

        // 计算总重量
        BigDecimal total = calculateTotalWeight(record);
        record.setTotalWeight(total);

        // 设置录入人
        if (record.getCreatedBy() == null || record.getCreatedBy().isEmpty()) {
            String realName = SecurityUtils.getCurrentRealName();
            record.setCreatedBy(realName != null ? realName : "系统");
        }

        logger.info("生成批次编号: {}, 医院: {}, 总重量: {}kg", batchNo, record.getHospitalNo(), total);
        return wasteRecordMapper.insert(record);
    }

    /**
     * 更新记录
     * @param record 记录信息
     * @return 影响行数
     */
    public int update(WasteRecord record) {
        // 重新计算总重量
        BigDecimal total = calculateTotalWeight(record);
        record.setTotalWeight(total);
        return wasteRecordMapper.update(record);
    }

    /**
     * 删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    public int delete(Long id) {
        return wasteRecordMapper.deleteById(id);
    }

    /**
     * 导出医废记录为Excel文件
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称
     * @param batchNo 批次编号
     * @return Excel文件字节数组
     * @throws IOException 写入异常
     */
    public byte[] exportToExcel(LocalDate startDate, LocalDate endDate, String region,
                                 String hospitalName, String batchNo) throws IOException {
        List<WasteRecord> records = wasteRecordMapper.findAllForExport(
            startDate, endDate, region, hospitalName, batchNo);

        logger.info("导出医废记录，共{}条数据", records.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("医废记录");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"批次编号", "医院名称", "地区", "车牌号", "人员",
                "感染性(kg)", "损伤性(kg)", "药物性(kg)", "化学性(kg)", "病理性(kg)",
                "总重量(kg)", "录入日期", "录入人"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < records.size(); i++) {
                WasteRecord record = records.get(i);
                Row row = sheet.createRow(i + 1);

                createCell(row, 0, record.getBatchNo(), dataStyle);
                createCell(row, 1, record.getHospitalName(), dataStyle);
                createCell(row, 2, getRegionName(record.getRegion()), dataStyle);
                createCell(row, 3, record.getPlateNumber(), dataStyle);
                createCell(row, 4, record.getStaffName(), dataStyle);
                createWeightCell(row, 5, record.getInfectiousWeight(), dataStyle);
                createWeightCell(row, 6, record.getInjuriousWeight(), dataStyle);
                createWeightCell(row, 7, record.getPharmaceuticalWeight(), dataStyle);
                createWeightCell(row, 8, record.getChemicalWeight(), dataStyle);
                createWeightCell(row, 9, record.getPathologicalWeight(), dataStyle);
                createWeightCell(row, 10, record.getTotalWeight(), dataStyle);
                createCell(row, 11, record.getRecordDate() != null ? record.getRecordDate().toString() : "", dataStyle);
                createCell(row, 12, record.getCreatedBy(), dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 创建表头样式
     * @param workbook 工作簿
     * @return 表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据样式
     * @param workbook 工作簿
     * @return 数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建文本单元格
     * @param row 行
     * @param col 列索引
     * @param value 值
     * @param style 样式
     */
    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 创建重量单元格
     * @param row 行
     * @param col 列索引
     * @param value 重量值
     * @param style 样式
     */
    private void createWeightCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("-");
        }
        cell.setCellStyle(style);
    }

    /**
     * 获取地区名称
     * @param code 地区编码
     * @return 地区名称
     */
    private String getRegionName(String code) {
        if (code == null || code.isEmpty()) {
            return "-";
        }
        return REGION_MAP.getOrDefault(code, "未知地区");
    }

    /**
     * 生成批次编号：YF + 日期(8位) + 序号(3位)
     * @param recordDate 录入日期
     * @return 批次编号
     */
    private String generateBatchNo(LocalDate recordDate) {
        String datePrefix = BATCH_PREFIX + recordDate.format(DATE_FORMATTER);
        String maxBatchNo = wasteRecordMapper.selectMaxBatchNoByDatePrefixForUpdate(datePrefix);

        if (maxBatchNo == null || maxBatchNo.isEmpty()) {
            return datePrefix + "001";
        }

        try {
            // 提取序号部分（最后3位）
            String seqPart = maxBatchNo.substring(maxBatchNo.length() - 3);
            int seq = Integer.parseInt(seqPart) + 1;
            return datePrefix + String.format("%03d", seq);
        } catch (NumberFormatException e) {
            logger.error("批次编号格式错误: {}", maxBatchNo);
            throw new BusinessException("批次编号生成失败：编号格式错误");
        }
    }

    /**
     * 计算总重量（五类废物之和）
     * @param record 记录信息
     * @return 总重量
     */
    private BigDecimal calculateTotalWeight(WasteRecord record) {
        BigDecimal total = BigDecimal.ZERO;
        if (record.getInfectiousWeight() != null) {
            total = total.add(record.getInfectiousWeight());
        }
        if (record.getInjuriousWeight() != null) {
            total = total.add(record.getInjuriousWeight());
        }
        if (record.getPharmaceuticalWeight() != null) {
            total = total.add(record.getPharmaceuticalWeight());
        }
        if (record.getChemicalWeight() != null) {
            total = total.add(record.getChemicalWeight());
        }
        if (record.getPathologicalWeight() != null) {
            total = total.add(record.getPathologicalWeight());
        }
        return total;
    }
}
