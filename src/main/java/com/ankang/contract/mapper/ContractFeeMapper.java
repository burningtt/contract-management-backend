package com.ankang.contract.mapper;

import com.ankang.contract.entity.ContractFee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractFeeMapper {
    ContractFee findById(Long id);
    List<ContractFee> findByInstitutionNo(@Param("institutionNo") String institutionNo);
    List<ContractFee> findAll();
    int insert(ContractFee contractFee);
    int update(ContractFee contractFee);
    int deleteById(Long id);
}
