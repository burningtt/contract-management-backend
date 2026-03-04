package com.ankang.contract.task;

import com.ankang.contract.entity.Contract;
import com.ankang.contract.mapper.ContractMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContractStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ContractStatusScheduler.class);
    private final ContractMapper contractMapper;

    public ContractStatusScheduler(ContractMapper contractMapper) {
        this.contractMapper = contractMapper;
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void updateContractStatus() {
        logger.info("开始执行合同状态更新定时任务");
        long startTime = System.currentTimeMillis();
        
        try {
            List<Contract> contracts = contractMapper.findAll();
            logger.info("共查询到 {} 条合同记录", contracts.size());
            
            int updatedCount = 0;
            int pendingCount = 0;
            int activeCount = 0;
            int inactiveCount = 0;
            
            for (Contract contract : contracts) {
                String newStatus = determineContractStatus(contract.getSignDate(), contract.getExpireDate());
                String currentStatus = contract.getContractStatus();
                
                if (!newStatus.equals(currentStatus)) {
                    contractMapper.updateStatus(contract.getId(), newStatus);
                    updatedCount++;
                    logger.debug("合同[{}]状态从 {} 更新为 {}", contract.getContractNo(), currentStatus, newStatus);
                }
                
                switch (newStatus) {
                    case "PENDING":
                        pendingCount++;
                        break;
                    case "ACTIVE":
                        activeCount++;
                        break;
                    case "INACTIVE":
                        inactiveCount++;
                        break;
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("合同状态更新定时任务执行完成 - 总记录: {}, 更新: {}, 未生效: {}, 活跃: {}, 失效: {}, 耗时: {}ms", 
                contracts.size(), updatedCount, pendingCount, activeCount, inactiveCount, (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("合同状态更新定时任务执行失败", e);
            throw e;
        }
    }

    String determineContractStatus(LocalDate signDate, LocalDate expireDate) {
        LocalDate today = LocalDate.now();
        
        if (signDate != null && signDate.isAfter(today)) {
            return "PENDING";
        }
        if (expireDate != null && expireDate.isBefore(today)) {
            return "INACTIVE";
        }
        return "ACTIVE";
    }
}
