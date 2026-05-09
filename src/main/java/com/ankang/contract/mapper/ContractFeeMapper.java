package com.ankang.contract.mapper;

import com.ankang.contract.entity.ContractFee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface ContractFeeMapper {
    ContractFee findById(Long id);
    java.util.List<ContractFee> findByInstitutionNo(@Param("institutionNo") String institutionNo);
    int insert(ContractFee contractFee);
    int update(ContractFee contractFee);
    int deleteById(Long id);
    Long count();
    BigDecimal sumTotalAmount();
    Long countByYear(@Param("year") Integer year);
    BigDecimal sumAmountByYear(@Param("year") Integer year);
    BigDecimal sumAmountByCreateTimeYear(@Param("year") Integer year);
    BigDecimal sumAmountByCreateTimeYearAndRegion(@Param("year") Integer year, @Param("region") String region);
}
