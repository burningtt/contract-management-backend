package com.ankang.contract.dto;

/**
 * 医院Excel导入DTO
 */
public class WasteHospitalImportDTO {

    /** 行号 */
    private int rowNumber;

    /** 医院名称 */
    private String hospitalName;

    /** 地区 */
    private String region;

    /** 是否验证通过 */
    private boolean valid;

    /** 验证消息 */
    private String validationMessage;

    public WasteHospitalImportDTO() {
        this.valid = true;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}
