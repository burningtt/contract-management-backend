package com.ankang.contract.entity;

import java.math.BigDecimal;

/**
 * 医废车辆实体类
 */
public class WasteVehicle extends BaseEntity {

    private Long id;

    /** 车辆编号（如V00001） */
    private String vehicleNo;

    /** 车牌号 */
    private String plateNumber;

    /** 车辆里程（公里） */
    private BigDecimal mileage;

    /** 状态：1-正常，0-已删除 */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public BigDecimal getMileage() {
        return mileage;
    }

    public void setMileage(BigDecimal mileage) {
        this.mileage = mileage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
