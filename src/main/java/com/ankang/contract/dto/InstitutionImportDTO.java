package com.ankang.contract.dto;

/**
 * 机构导入DTO
 */
public class InstitutionImportDTO {
    private int rowNumber;
    private String region;
    private String institutionName;
    private String contactPhone;
    private String institutionType;
    private String contactPerson;
    private Integer actualBeds;
    private Integer openBeds;
    private String remark;
    private boolean valid;
    private String validationMessage;

    public InstitutionImportDTO() {
        this.valid = true;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(String institutionType) {
        this.institutionType = institutionType;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public Integer getActualBeds() {
        return actualBeds;
    }

    public void setActualBeds(Integer actualBeds) {
        this.actualBeds = actualBeds;
    }

    public Integer getOpenBeds() {
        return openBeds;
    }

    public void setOpenBeds(Integer openBeds) {
        this.openBeds = openBeds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
