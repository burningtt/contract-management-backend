package com.ankang.contract.dto;

import java.math.BigDecimal;

public class FeeStatisticsDTO {
    private String institutionNo;
    private String institutionName;
    private String region;
    private BigDecimal annualDue;
    private BigDecimal annualCollected;
    private BigDecimal annualArrears;
    private String collectionRate;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getAnnualDue() {
        return annualDue;
    }

    public void setAnnualDue(BigDecimal annualDue) {
        this.annualDue = annualDue;
    }

    public BigDecimal getAnnualCollected() {
        return annualCollected;
    }

    public void setAnnualCollected(BigDecimal annualCollected) {
        this.annualCollected = annualCollected;
    }

    public BigDecimal getAnnualArrears() {
        return annualArrears;
    }

    public void setAnnualArrears(BigDecimal annualArrears) {
        this.annualArrears = annualArrears;
    }

    public String getCollectionRate() {
        return collectionRate;
    }

    public void setCollectionRate(String collectionRate) {
        this.collectionRate = collectionRate;
    }
}
