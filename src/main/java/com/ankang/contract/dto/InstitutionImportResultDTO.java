package com.ankang.contract.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 机构导入结果DTO
 */
public class InstitutionImportResultDTO {
    private int totalCount;
    private int successCount;
    private int failCount;
    private List<ImportError> errors;
    private List<InstitutionImportDTO> validData;

    public InstitutionImportResultDTO() {
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

    public List<InstitutionImportDTO> getValidData() {
        return validData;
    }

    public void setValidData(List<InstitutionImportDTO> validData) {
        this.validData = validData;
    }

    public static class ImportError {
        private int rowNumber;
        private String institutionName;
        private String field;
        private String errorMessage;

        public ImportError() {
        }

        public ImportError(int rowNumber, String institutionName, String field, String errorMessage) {
            this.rowNumber = rowNumber;
            this.institutionName = institutionName;
            this.field = field;
            this.errorMessage = errorMessage;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getInstitutionName() {
            return institutionName;
        }

        public void setInstitutionName(String institutionName) {
            this.institutionName = institutionName;
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
