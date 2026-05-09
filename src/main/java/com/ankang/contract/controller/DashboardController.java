package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.mapper.InstitutionMapper;
import com.ankang.contract.mapper.ContractMapper;
import com.ankang.contract.mapper.ContractFeeMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页统计控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final InstitutionMapper institutionMapper;
    private final ContractMapper contractMapper;
    private final ContractFeeMapper contractFeeMapper;

    public DashboardController(InstitutionMapper institutionMapper, ContractMapper contractMapper, ContractFeeMapper contractFeeMapper) {
        this.institutionMapper = institutionMapper;
        this.contractMapper = contractMapper;
        this.contractFeeMapper = contractFeeMapper;
    }

    /**
     * 获取首页统计数据
     * @param region 地区参数，为空时查询全部
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam(value = "region", required = false) String region) {
        Map<String, Object> statistics = new HashMap<>();
        
        Integer currentYear = LocalDate.now().getYear();
        
        if (region != null && !region.isEmpty()) {
            Long institutionCount = institutionMapper.count(null, null, "NORMAL", region);
            statistics.put("institutionCount", institutionCount);
            
            Long contractCount = contractMapper.countByYearAndRegion(currentYear, region);
            statistics.put("contractCount", contractCount != null ? contractCount : 0L);
            
            BigDecimal totalAmount = contractMapper.sumAmountByYearAndRegion(currentYear, region);
            statistics.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
            
            BigDecimal feeAmount = contractFeeMapper.sumAmountByCreateTimeYearAndRegion(currentYear, region);
            statistics.put("feeAmount", feeAmount != null ? feeAmount : BigDecimal.ZERO);
        } else {
            Long institutionCount = institutionMapper.count(null, null, "NORMAL", null);
            statistics.put("institutionCount", institutionCount);
            
            Long contractCount = contractMapper.countByYear(currentYear);
            statistics.put("contractCount", contractCount != null ? contractCount : 0L);
            
            BigDecimal totalAmount = contractMapper.sumAmountByYear(currentYear);
            statistics.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
            
            BigDecimal feeAmount = contractFeeMapper.sumAmountByCreateTimeYear(currentYear);
            statistics.put("feeAmount", feeAmount != null ? feeAmount : BigDecimal.ZERO);
        }
        
        return ApiResponse.success(statistics);
    }
}
