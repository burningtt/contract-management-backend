package com.ankang.contract.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 医院导入结果DTO
 */
public class WasteHospitalImportResultDTO {

    private int totalCount;
    private int successCount;
    private int failCount;
    private List<ImportError> errors;
    private List<WasteHospitalImportDTO> validData;

    public WasteHospitalImportResultDTO() {
        this.errors = new ArrayList<>();
        this.validData = new ArrayList<>();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<ImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportError> errors) {
        this.errors = errors;
    }

    public List<WasteHospitalImportDTO> getValidData() {
        return validData;
    }

    public void setValidData(List<WasteHospitalImportDTO> validData) {
        this.validData = validData;
    }

    /**
     * 导入错误信息
     */
    public static class ImportError {
        private int rowNumber;
        private String hospitalName;
        private String field;
        private String errorMessage;

        public ImportError() {
        }

        public ImportError(int rowNumber, String hospitalName, String field, String errorMessage) {
            this.rowNumber = rowNumber;
            this.hospitalName = hospitalName;
            this.field = field;
            this.errorMessage = errorMessage;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getHospitalName() {
            return hospitalName;
        }

        public void setHospitalName(String hospitalName) {
            this.hospitalName = hospitalName;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
