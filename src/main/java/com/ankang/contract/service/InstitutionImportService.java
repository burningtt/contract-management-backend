package com.ankang.contract.service;

import com.ankang.contract.dto.InstitutionImportDTO;
import com.ankang.contract.dto.InstitutionImportResultDTO;
import com.ankang.contract.entity.Institution;
import com.ankang.contract.mapper.InstitutionMapper;
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
import java.util.regex.Pattern;

/**
 * 机构导入服务
 */
@Service
public class InstitutionImportService {

    private static final Logger logger = LoggerFactory.getLogger(InstitutionImportService.class);

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_RECORDS = 1000;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\d{3,4}-)?\\d{7,8}$|^1[3-9]\\d{9}$");

    private final InstitutionMapper institutionMapper;

    public InstitutionImportService(InstitutionMapper institutionMapper) {
        this.institutionMapper = institutionMapper;
    }

    /**
     * 解析Excel文件并验证数据
     * @param file 上传的Excel文件
     * @return 解析结果
     */
    public InstitutionImportResultDTO parseAndValidate(MultipartFile file) throws IOException {
        validateFile(file);

        InstitutionImportResultDTO result = new InstitutionImportResultDTO();
        List<InstitutionImportDTO> dataList = new ArrayList<>();

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

                InstitutionImportDTO dto = parseRow(row, columnIndexMap, i);
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
     * 批量导入机构数据
     * @param dataList 待导入的数据列表
     * @param operator 操作人
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public InstitutionImportResultDTO importData(List<InstitutionImportDTO> dataList, String operator) {
        InstitutionImportResultDTO result = new InstitutionImportResultDTO();
        result.setTotalCount(dataList.size());

        int successCount = 0;
        int failCount = 0;

        for (InstitutionImportDTO dto : dataList) {
            try {
                Institution institution = convertToEntity(dto, operator);
                institutionMapper.insert(institution);
                successCount++;
            } catch (Exception e) {
                failCount++;
                result.getErrors().add(new InstitutionImportResultDTO.ImportError(
                    dto.getRowNumber(),
                    dto.getInstitutionName(),
                    "数据库",
                    "导入失败: " + e.getMessage()
                ));
                logger.error("导入机构数据失败: {}", dto.getInstitutionName(), e);
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(failCount);

        return result;
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
        String[] requiredHeaders = {"地区", "机构名称", "联系电话", "机构类型", "联系人", "实际床位", "开放床位", "备注"};

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

    private InstitutionImportDTO parseRow(Row row, Map<String, Integer> columnIndexMap, int rowNumber) {
        InstitutionImportDTO dto = new InstitutionImportDTO();
        dto.setRowNumber(rowNumber);

        dto.setRegion(getCellValueByHeader(row, columnIndexMap, "地区"));
        dto.setInstitutionName(getCellValueByHeader(row, columnIndexMap, "机构名称"));
        dto.setContactPhone(getCellValueByHeader(row, columnIndexMap, "联系电话"));
        dto.setInstitutionType(getCellValueByHeader(row, columnIndexMap, "机构类型"));
        dto.setContactPerson(getCellValueByHeader(row, columnIndexMap, "联系人"));
        dto.setActualBeds(getCellValueAsInteger(row, columnIndexMap, "实际床位"));
        dto.setOpenBeds(getCellValueAsInteger(row, columnIndexMap, "开放床位"));
        dto.setRemark(getCellValueByHeader(row, columnIndexMap, "备注"));

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

    private Integer getCellValueAsInteger(Row row, Map<String, Integer> columnIndexMap, String header) {
        String value = getCellValueByHeader(row, columnIndexMap, header);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            int intValue = Integer.parseInt(value);
            return intValue >= 0 ? intValue : null;
        } catch (NumberFormatException e) {
            return null;
        }
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

    private void validateData(List<InstitutionImportDTO> dataList, InstitutionImportResultDTO result) {
        Set<String> existingNames = new HashSet<>();
        List<Institution> existingInstitutions = institutionMapper.findAll();
        for (Institution inst : existingInstitutions) {
            existingNames.add(inst.getInstitutionName());
        }

        Set<String> importedNames = new HashSet<>();

        for (InstitutionImportDTO dto : dataList) {
            List<String> errors = new ArrayList<>();

            if (dto.getRegion() == null || dto.getRegion().isEmpty()) {
                errors.add("地区不能为空");
            }
            if (dto.getInstitutionName() == null || dto.getInstitutionName().isEmpty()) {
                errors.add("机构名称不能为空");
            }

            if (dto.getContactPhone() != null && !dto.getContactPhone().isEmpty()) {
                if (!PHONE_PATTERN.matcher(dto.getContactPhone()).matches()) {
                    errors.add("联系电话格式不正确");
                }
            }

            if (dto.getActualBeds() != null && dto.getActualBeds() < 0) {
                errors.add("实际床位必须为非负整数");
            }
            if (dto.getOpenBeds() != null && dto.getOpenBeds() < 0) {
                errors.add("开放床位必须为非负整数");
            }

            if (dto.getInstitutionName() != null && !dto.getInstitutionName().isEmpty()) {
                if (existingNames.contains(dto.getInstitutionName())) {
                    errors.add("机构名称已存在于数据库中");
                }
                if (importedNames.contains(dto.getInstitutionName())) {
                    errors.add("机构名称在导入文件中重复");
                }
                importedNames.add(dto.getInstitutionName());
            }

            if (!errors.isEmpty()) {
                dto.setValid(false);
                dto.setValidationMessage(String.join("; ", errors));
                result.getErrors().add(new InstitutionImportResultDTO.ImportError(
                    dto.getRowNumber(),
                    dto.getInstitutionName(),
                    "验证",
                    dto.getValidationMessage()
                ));
            } else {
                result.getValidData().add(dto);
            }
        }

        result.setSuccessCount(result.getValidData().size());
        result.setFailCount(result.getErrors().size());
    }

    private Institution convertToEntity(InstitutionImportDTO dto, String operator) {
        Institution institution = new Institution();
        institution.setRegion(dto.getRegion());
        institution.setInstitutionName(dto.getInstitutionName());
        institution.setContactPhone(dto.getContactPhone());
        institution.setInstitutionType(dto.getInstitutionType());
        institution.setContactPerson(dto.getContactPerson());
        institution.setActualBeds(dto.getActualBeds());
        institution.setOpenBeds(dto.getOpenBeds());
        institution.setRemark(dto.getRemark());
        institution.setInstitutionStatus("NORMAL");
        institution.setCreatedBy(operator);

        String maxNo = institutionMapper.selectMaxInstitutionNoForUpdate();
        String newNo = generateNextInstitutionNo(maxNo);
        institution.setInstitutionNo(newNo);

        return institution;
    }

    private String generateNextInstitutionNo(String maxNo) {
        if (maxNo == null || maxNo.isEmpty()) {
            return "100001";
        }
        try {
            long num = Long.parseLong(maxNo);
            return String.format("%06d", num + 1);
        } catch (Exception e) {
            return "100001";
        }
    }
}
