package com.ankang.contract.entity;

public class Institution extends BaseEntity {
    private Long id;
    private String institutionNo;
    private String institutionName;
    private String institutionType;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String region;
    private String address;
    private String institutionStatus;
    private Integer actualBeds;
    private Integer openBeds;
    private String createdBy;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstitutionNo() {
        return institutionNo;
    }

    public void setInstitutionNo(String institutionNo) {
        this.institutionNo = institutionNo;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInstitutionStatus() {
        return institutionStatus;
    }

    public void setInstitutionStatus(String institutionStatus) {
        this.institutionStatus = institutionStatus;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
