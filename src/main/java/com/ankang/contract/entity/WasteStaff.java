package com.ankang.contract.entity;

/**
 * 车队人员实体类
 */
public class WasteStaff extends BaseEntity {

    private Long id;

    /** 人员编号（如S00001） */
    private String staffNo;

    /** 姓名 */
    private String staffName;

    /** 电话 */
    private String phone;

    /** 证件号 */
    private String idCard;

    /** 岗位 */
    private String position;

    /** 状态：1-正常，0-已删除 */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
