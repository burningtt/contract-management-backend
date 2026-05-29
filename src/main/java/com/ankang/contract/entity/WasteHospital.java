package com.ankang.contract.entity;

/**
 * 医废医院实体类
 */
public class WasteHospital extends BaseEntity {

    private Long id;

    /** 医院编号（如H00001） */
    private String hospitalNo;

    /** 医院名称 */
    private String hospitalName;

    /** 地区编码（忻州市14个区县编码） */
    private String region;

    /** 状态：1-正常，0-已删除 */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHospitalNo() {
        return hospitalNo;
    }

    public void setHospitalNo(String hospitalNo) {
        this.hospitalNo = hospitalNo;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
