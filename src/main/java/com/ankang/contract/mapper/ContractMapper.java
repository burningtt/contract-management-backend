package com.ankang.contract.mapper;

import com.ankang.contract.dto.ContractQueryDTO;
import com.ankang.contract.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface ContractMapper {
    Contract findById(Long id);
    String findInstitutionNoById(Long id);
    java.util.List<Contract> findAll();
    java.util.List<Contract> findByInstitutionNo(String institutionNo);
    java.util.List<ContractQueryDTO> findInstitutionContractsWithPriority(int offset, int size);
    Long countInstitutionContractsWithPriority();
    java.util.List<ContractQueryDTO> searchInstitutionContracts(@Param("institutionNo") String institutionNo, @Param("region") String region, @Param("year") Integer year, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
    Long countSearchInstitutionContracts(@Param("institutionNo") String institutionNo, @Param("region") String region, @Param("year") Integer year, @Param("keyword") String keyword);
    String findMaxContractNoByYearAndRegion(@Param("year") int year, @Param("region") String region);
    java.util.List<Contract> findByInstitutionNoForDateCheck(@Param("institutionNo") String institutionNo, @Param("excludeId") Long excludeId);
    int insert(Contract contract);
    int update(Contract contract);
    int updateStatus(@Param("id") Long id, @Param("contractStatus") String contractStatus);
    int deleteById(Long id);
    Long countByYear(@Param("year") Integer year);
    BigDecimal sumAmountByYear(@Param("year") Integer year);
    Long countByYearAndRegion(@Param("year") Integer year, @Param("region") String region);
    BigDecimal sumAmountByYearAndRegion(@Param("year") Integer year, @Param("region") String region);
}
