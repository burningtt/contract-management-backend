package com.ankang.contract.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractStatusSchedulerTest {

    private final ContractStatusScheduler scheduler = new ContractStatusScheduler(null);

    @Test
    @DisplayName("当前日期在签订日期之前 - 应返回PENDING")
    void determineContractStatus_BeforeSignDate_ReturnsPending() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today.plusDays(10);
        LocalDate expireDate = today.plusDays(100);
        
        String status = scheduler.determineContractStatus(signDate, expireDate);
        
        assertEquals("PENDING", status);
    }

    @Test
    @DisplayName("当前日期在签订日期和到期日期之间 - 应返回ACTIVE")
    void determineContractStatus_BetweenSignAndExpireDate_ReturnsActive() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today.minusDays(10);
        LocalDate expireDate = today.plusDays(100);
        
        String status = scheduler.determineContractStatus(signDate, expireDate);
        
        assertEquals("ACTIVE", status);
    }

    @Test
    @DisplayName("当前日期等于签订日期 - 应返回ACTIVE")
    void determineContractStatus_EqualSignDate_ReturnsActive() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today;
        LocalDate expireDate = today.plusDays(100);
        
        String status = scheduler.determineContractStatus(signDate, expireDate);
        
        assertEquals("ACTIVE", status);
    }

    @Test
    @DisplayName("当前日期等于到期日期 - 应返回ACTIVE")
    void determineContractStatus_EqualExpireDate_ReturnsActive() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today.minusDays(100);
        LocalDate expireDate = today;
        
        String status = scheduler.determineContractStatus(signDate, expireDate);
        
        assertEquals("ACTIVE", status);
    }

    @Test
    @DisplayName("当前日期在到期日期之后 - 应返回INACTIVE")
    void determineContractStatus_AfterExpireDate_ReturnsInactive() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today.minusDays(100);
        LocalDate expireDate = today.minusDays(10);
        
        String status = scheduler.determineContractStatus(signDate, expireDate);
        
        assertEquals("INACTIVE", status);
    }

    @Test
    @DisplayName("签订日期为null - 应返回ACTIVE")
    void determineContractStatus_NullSignDate_ReturnsActive() {
        LocalDate today = LocalDate.now();
        LocalDate expireDate = today.plusDays(100);
        
        String status = scheduler.determineContractStatus(null, expireDate);
        
        assertEquals("ACTIVE", status);
    }

    @Test
    @DisplayName("到期日期为null - 应返回ACTIVE")
    void determineContractStatus_NullExpireDate_ReturnsActive() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today.minusDays(10);
        
        String status = scheduler.determineContractStatus(signDate, null);
        
        assertEquals("ACTIVE", status);
    }

    @Test
    @DisplayName("签订日期和到期日期都为null - 应返回ACTIVE")
    void determineContractStatus_BothDatesNull_ReturnsActive() {
        String status = scheduler.determineContractStatus(null, null);
        
        assertEquals("ACTIVE", status);
    }

    @Test
    @DisplayName("签订日期在未来且到期日期为null - 应返回PENDING")
    void determineContractStatus_FutureSignDateNullExpireDate_ReturnsPending() {
        LocalDate today = LocalDate.now();
        LocalDate signDate = today.plusDays(10);
        
        String status = scheduler.determineContractStatus(signDate, null);
        
        assertEquals("PENDING", status);
    }

    @Test
    @DisplayName("到期日期在过去且签订日期为null - 应返回INACTIVE")
    void determineContractStatus_NullSignDatePastExpireDate_ReturnsInactive() {
        LocalDate today = LocalDate.now();
        LocalDate expireDate = today.minusDays(10);
        
        String status = scheduler.determineContractStatus(null, expireDate);
        
        assertEquals("INACTIVE", status);
    }
}
