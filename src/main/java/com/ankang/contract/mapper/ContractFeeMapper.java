package com.ankang.contract.mapper;

import com.ankang.contract.entity.ContractFee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContractFeeMapper {
    ContractFee findById(Long id);
    java.util.List<ContractFee> findByInstitutionNo(@Param("institutionNo") String institutionNo);
    int insert(ContractFee contractFee);
    int update(ContractFee contractFee);
    int deleteById(Long id);
}
