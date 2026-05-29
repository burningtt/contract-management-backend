package com.ankang.contract.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 医废重量记录实体类
 */
public class WasteRecord extends BaseEntity {

    private Long id;

    /** 批次编号（如YF20260522001） */
    private String batchNo;

    /** 医院编号 */
    private String hospitalNo;

    /** 地区编码 */
    private String region;

    /** 车辆编号 */
    private String vehicleNo;

    /** 人员编号（逗号分隔，支持多选，如S00001,S00002） */
    private String staffNos;

    /** 感染性重量(kg) */
    private BigDecimal infectiousWeight;

    /** 损伤性重量(kg) */
    private BigDecimal injuriousWeight;

    /** 药物性重量(kg) */
    private BigDecimal pharmaceuticalWeight;

    /** 化学性重量(kg) */
    private BigDecimal chemicalWeight;

    /** 病理性重量(kg) */
    private BigDecimal pathologicalWeight;

    /** 总重量(kg) */
    private BigDecimal totalWeight;

    /** 录入日期 */
    private LocalDate recordDate;

    /** 录入人 */
    private String createdBy;

    /** 医院名称（查询时关联填充，非数据库字段） */
    private String hospitalName;

    /** 车牌号（查询时关联填充，非数据库字段） */
    private String plateNumber;

    /** 人员姓名（查询时关联填充，非数据库字段） */
    private String staffName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getHospitalNo() {
        return hospitalNo;
    }

    public void setHospitalNo(String hospitalNo) {
        this.hospitalNo = hospitalNo;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getStaffNos() {
        return staffNos;
    }

    public void setStaffNos(String staffNos) {
        this.staffNos = staffNos;
    }

    public BigDecimal getInfectiousWeight() {
        return infectiousWeight;
    }

    public void setInfectiousWeight(BigDecimal infectiousWeight) {
        this.infectiousWeight = infectiousWeight;
    }

    public BigDecimal getInjuriousWeight() {
        return injuriousWeight;
    }

    public void setInjuriousWeight(BigDecimal injuriousWeight) {
        this.injuriousWeight = injuriousWeight;
    }

    public BigDecimal getPharmaceuticalWeight() {
        return pharmaceuticalWeight;
    }

    public void setPharmaceuticalWeight(BigDecimal pharmaceuticalWeight) {
        this.pharmaceuticalWeight = pharmaceuticalWeight;
    }

    public BigDecimal getChemicalWeight() {
        return chemicalWeight;
    }

    public void setChemicalWeight(BigDecimal chemicalWeight) {
        this.chemicalWeight = chemicalWeight;
    }

    public BigDecimal getPathologicalWeight() {
        return pathologicalWeight;
    }

    public void setPathologicalWeight(BigDecimal pathologicalWeight) {
        this.pathologicalWeight = pathologicalWeight;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}
