package com.ankang.contract.task;

import com.ankang.contract.entity.Contract;
import com.ankang.contract.mapper.ContractMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractStatusSchedulerIntegrationTest {

    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private ContractStatusScheduler scheduler;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    @Test
    @DisplayName("空合同列表 - 不执行更新操作")
    void updateContractStatus_EmptyList_NoUpdate() {
        when(contractMapper.findAll()).thenReturn(Collections.emptyList());

        scheduler.updateContractStatus();

        verify(contractMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    @DisplayName("合同状态需要更新 - 执行更新操作")
    void updateContractStatus_StatusChanged_ExecutesUpdate() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNo("2025忻府区0001");
        contract.setSignDate(today.minusDays(100));
        contract.setExpireDate(today.minusDays(10));
        contract.setContractStatus("ACTIVE");

        when(contractMapper.findAll()).thenReturn(List.of(contract));

        scheduler.updateContractStatus();

        verify(contractMapper, times(1)).updateStatus(1L, "INACTIVE");
    }

    @Test
    @DisplayName("合同状态无需更新 - 不执行更新操作")
    void updateContractStatus_StatusUnchanged_NoUpdate() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNo("2025忻府区0001");
        contract.setSignDate(today.minusDays(10));
        contract.setExpireDate(today.plusDays(100));
        contract.setContractStatus("ACTIVE");

        when(contractMapper.findAll()).thenReturn(List.of(contract));

        scheduler.updateContractStatus();

        verify(contractMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    @DisplayName("多个合同混合状态 - 只更新需要变更的记录")
    void updateContractStatus_MixedContracts_UpdatesOnlyChanged() {
        Contract activeContract = new Contract();
        activeContract.setId(1L);
        activeContract.setContractNo("2025忻府区0001");
        activeContract.setSignDate(today.minusDays(10));
        activeContract.setExpireDate(today.plusDays(100));
        activeContract.setContractStatus("ACTIVE");

        Contract expiredContract = new Contract();
        expiredContract.setId(2L);
        expiredContract.setContractNo("2025忻府区0002");
        expiredContract.setSignDate(today.minusDays(100));
        expiredContract.setExpireDate(today.minusDays(10));
        expiredContract.setContractStatus("ACTIVE");

        Contract pendingContract = new Contract();
        pendingContract.setId(3L);
        pendingContract.setContractNo("2025忻府区0003");
        pendingContract.setSignDate(today.plusDays(10));
        pendingContract.setExpireDate(today.plusDays(100));
        pendingContract.setContractStatus("PENDING");

        when(contractMapper.findAll()).thenReturn(Arrays.asList(activeContract, expiredContract, pendingContract));

        scheduler.updateContractStatus();

        verify(contractMapper, times(1)).updateStatus(eq(2L), eq("INACTIVE"));
        verify(contractMapper, never()).updateStatus(eq(1L), anyString());
        verify(contractMapper, never()).updateStatus(eq(3L), anyString());
    }

    @Test
    @DisplayName("PENDING合同到期 - 状态变更为ACTIVE")
    void updateContractStatus_PendingBecomesActive_UpdatesStatus() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNo("2025忻府区0001");
        contract.setSignDate(today);
        contract.setExpireDate(today.plusDays(100));
        contract.setContractStatus("PENDING");

        when(contractMapper.findAll()).thenReturn(List.of(contract));

        scheduler.updateContractStatus();

        verify(contractMapper, times(1)).updateStatus(eq(1L), eq("ACTIVE"));
    }

    @Test
    @DisplayName("ACTIVE合同过期 - 状态变更为INACTIVE")
    void updateContractStatus_ActiveBecomesInactive_UpdatesStatus() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNo("2025忻府区0001");
        contract.setSignDate(today.minusDays(100));
        contract.setExpireDate(today.minusDays(1));
        contract.setContractStatus("ACTIVE");

        when(contractMapper.findAll()).thenReturn(List.of(contract));

        scheduler.updateContractStatus();

        verify(contractMapper, times(1)).updateStatus(eq(1L), eq("INACTIVE"));
    }
}
