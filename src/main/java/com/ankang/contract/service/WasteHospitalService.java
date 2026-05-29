package com.ankang.contract.service;

import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.dto.WasteHospitalImportDTO;
import com.ankang.contract.dto.WasteHospitalImportResultDTO;
import com.ankang.contract.entity.WasteHospital;
import com.ankang.contract.exception.BusinessException;
import com.ankang.contract.mapper.WasteHospitalMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 医院管理服务
 */
@Service
public class WasteHospitalService {

    private static final Logger logger = LoggerFactory.getLogger(WasteHospitalService.class);
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_RECORDS = 1000;

    /** 忻州市14个区县名称 */
    private static final Set<String> VALID_REGIONS = Set.of(
        "忻府区", "原平市", "定襄县", "五台县", "代县", "繁峙县",
        "静乐县", "宁武县", "神池县", "五寨县", "岢岚县", "保德县", "河曲县", "偏关县"
    );

    /** 地区中文名称到编码的映射 */
    private static final Map<String, String> REGION_CODE_MAP = Map.ofEntries(
        Map.entry("忻府区", "XFQ"),
        Map.entry("原平市", "YPS"),
        Map.entry("定襄县", "DXX"),
        Map.entry("五台县", "WTX"),
        Map.entry("代县", "DZX"),
        Map.entry("繁峙县", "FSX"),
        Map.entry("静乐县", "JLX"),
        Map.entry("宁武县", "NWX"),
        Map.entry("神池县", "SCX"),
        Map.entry("五寨县", "WZX"),
        Map.entry("岢岚县", "KLX"),
        Map.entry("保德县", "BDX"),
        Map.entry("河曲县", "HQX"),
        Map.entry("偏关县", "PGX")
    );

    private final WasteHospitalMapper wasteHospitalMapper;

    public WasteHospitalService(WasteHospitalMapper wasteHospitalMapper) {
        this.wasteHospitalMapper = wasteHospitalMapper;
    }

    /**
     * 根据ID查询医院
     * @param id 医院ID
     * @return 医院信息
     */
    public WasteHospital findById(Long id) {
        return wasteHospitalMapper.findById(id);
    }

    /**
     * 查询所有医院
     * @return 医院列表
     */
    public List<WasteHospital> findAll() {
        return wasteHospitalMapper.findAll();
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
    public PageResponse<WasteHospital> findByPage(int page, int size, String hospitalName, String region, Integer status) {
        int offset = (page - 1) * size;
        List<WasteHospital> rows = wasteHospitalMapper.findByPage(offset, size, hospitalName, region, status);
        Long total = wasteHospitalMapper.count(hospitalName, region, status);
        return new PageResponse<>(rows, total);
    }

    /**
     * 新增医院
     * @param hospital 医院信息
     * @return 影响行数
     */
    @Transactional
    public int create(WasteHospital hospital) {
        String maxNo = wasteHospitalMapper.selectMaxHospitalNoForUpdate();
        String newNo = generateNextNo(maxNo, "H");
        hospital.setHospitalNo(newNo);
        logger.info("生成医院编号: {}", newNo);
        return wasteHospitalMapper.insert(hospital);
    }

    /**
     * 更新医院
     * @param hospital 医院信息
     * @return 影响行数
     */
    public int update(WasteHospital hospital) {
        return wasteHospitalMapper.update(hospital);
    }

    /**
     * 停用医院（逻辑删除）
     * @param id 医院ID
     * @return 影响行数
     */
    public int delete(Long id) {
        return wasteHospitalMapper.deleteById(id);
    }

    /**
     * 启用医院
     * @param id 医院ID
     * @return 影响行数
     */
    public int enable(Long id) {
        return wasteHospitalMapper.enableById(id);
    }

    /**
     * 解析并验证Excel文件
     * @param file 上传的Excel文件
     * @return 解析结果
     * @throws IOException 文件读取异常
     */
    public WasteHospitalImportResultDTO parseAndValidate(MultipartFile file) throws IOException {
        validateFile(file);

        WasteHospitalImportResultDTO result = new WasteHospitalImportResultDTO();
        List<WasteHospitalImportDTO> dataList = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            if (rowCount < 2) {
                throw new IllegalArgumentException("Excel文件至少需要包含标题行和一行数据");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndexMap = buildColumnIndexMap(headerRow);

            for (int i = 1; i < rowCount && i <= MAX_RECORDS; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                WasteHospitalImportDTO dto = parseRow(row, columnIndexMap, i);
                dataList.add(dto);
            }

            result.setTotalCount(dataList.size());
            validateData(dataList, result);

        } catch (Exception e) {
            logger.error("解析Excel文件失败", e);
            throw new IOException("解析Excel文件失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 批量导入医院数据
     * @param dataList 待导入的数据列表
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public WasteHospitalImportResultDTO importData(List<WasteHospitalImportDTO> dataList) {
        WasteHospitalImportResultDTO result = new WasteHospitalImportResultDTO();
        result.setTotalCount(dataList.size());

        int successCount = 0;
        int failCount = 0;

        for (WasteHospitalImportDTO dto : dataList) {
            try {
                WasteHospital hospital = convertToEntity(dto);
                wasteHospitalMapper.insert(hospital);
                successCount++;
            } catch (Exception e) {
                failCount++;
                result.getErrors().add(new WasteHospitalImportResultDTO.ImportError(
                    dto.getRowNumber(), dto.getHospitalName(), "数据库", "导入失败: " + e.getMessage()
                ));
                logger.error("导入医院数据失败: {}", dto.getHospitalName(), e);
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        return result;
    }

    /**
     * 生成导入模板
     * @return 模板文件字节数组
     * @throws IOException 生成异常
     */
    public byte[] generateTemplate() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("医院导入模板");

            String[] headers = {"医院名称", "地区"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);

                sheet.setColumnWidth(i, 20 * 256);
            }

            // 示例行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("示例医院");
            exampleRow.createCell(1).setCellValue("忻府区");

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("仅支持.xlsx格式的Excel文件");
        }
    }

    private Map<String, Integer> buildColumnIndexMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        String[] requiredHeaders = {"医院名称", "地区"};

        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell).trim();
            for (String required : requiredHeaders) {
                if (headerName.equals(required)) {
                    map.put(required, cell.getColumnIndex());
                    break;
                }
            }
        }
        return map;
    }

    private WasteHospitalImportDTO parseRow(Row row, Map<String, Integer> columnIndexMap, int rowNumber) {
        WasteHospitalImportDTO dto = new WasteHospitalImportDTO();
        dto.setRowNumber(rowNumber);
        dto.setHospitalName(getCellValueByHeader(row, columnIndexMap, "医院名称"));
        dto.setRegion(getCellValueByHeader(row, columnIndexMap, "地区"));
        return dto;
    }

    private String getCellValueByHeader(Row row, Map<String, Integer> columnIndexMap, String header) {
        Integer index = columnIndexMap.get(header);
        if (index == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        return cell == null ? null : getCellValueAsString(cell).trim();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private void validateData(List<WasteHospitalImportDTO> dataList, WasteHospitalImportResultDTO result) {
        // 获取已有医院名称
        Set<String> existingNames = new HashSet<>();
        List<WasteHospital> existingHospitals = wasteHospitalMapper.findAll();
        for (WasteHospital h : existingHospitals) {
            existingNames.add(h.getHospitalName());
        }

        Set<String> importedNames = new HashSet<>();

        for (WasteHospitalImportDTO dto : dataList) {
            List<String> errors = new ArrayList<>();

            if (dto.getHospitalName() == null || dto.getHospitalName().isEmpty()) {
                errors.add("医院名称不能为空");
            }
            if (dto.getRegion() == null || dto.getRegion().isEmpty()) {
                errors.add("地区不能为空");
            } else if (!VALID_REGIONS.contains(dto.getRegion())) {
                errors.add("地区不在忻州市14个区县范围内");
            }

            if (dto.getHospitalName() != null && !dto.getHospitalName().isEmpty()) {
                if (existingNames.contains(dto.getHospitalName())) {
                    errors.add("医院名称已存在于数据库中");
                }
                if (importedNames.contains(dto.getHospitalName())) {
                    errors.add("医院名称在导入文件中重复");
                }
                importedNames.add(dto.getHospitalName());
            }

            if (!errors.isEmpty()) {
                dto.setValid(false);
                dto.setValidationMessage(String.join("; ", errors));
                result.getErrors().add(new WasteHospitalImportResultDTO.ImportError(
                    dto.getRowNumber(), dto.getHospitalName(), "验证", dto.getValidationMessage()
                ));
            } else {
                result.getValidData().add(dto);
            }
        }

        result.setSuccessCount(result.getValidData().size());
        result.setFailCount(result.getErrors().size());
    }

    private WasteHospital convertToEntity(WasteHospitalImportDTO dto) {
        WasteHospital hospital = new WasteHospital();
        hospital.setHospitalName(dto.getHospitalName());
        // 将地区中文名称转换为编码存储
        hospital.setRegion(REGION_CODE_MAP.getOrDefault(dto.getRegion(), dto.getRegion()));

        // 生成编号
        String maxNo = wasteHospitalMapper.selectMaxHospitalNoForUpdate();
        String newNo = generateNextNo(maxNo, "H");
        hospital.setHospitalNo(newNo);

        return hospital;
    }

    private String generateNextNo(String maxNo, String prefix) {
        if (maxNo == null || maxNo.isEmpty()) {
            return prefix + "00001";
        }
        try {
            String numPart = maxNo.substring(prefix.length());
            long num = Long.parseLong(numPart) + 1;
            return prefix + String.format("%05d", num);
        } catch (NumberFormatException e) {
            logger.error("编号格式错误: {}", maxNo);
            throw new BusinessException("编号生成失败：编号格式错误");
        }
    }
}
