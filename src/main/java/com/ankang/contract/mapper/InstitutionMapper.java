package com.ankang.contract.mapper;

import com.ankang.contract.dto.FeeStatisticsDTO;
import com.ankang.contract.entity.Institution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InstitutionMapper {
    Institution findById(Long id);
    Institution findByInstitutionNo(String institutionNo);
    java.util.List<Institution> findAll();
    java.util.List<Institution> findByPage(int offset, int size, String institutionName, String institutionType, String institutionStatus, String region);
    Long count(String institutionName, String institutionType, String institutionStatus, String region);
    java.util.List<Institution> findByType(String institutionType);
    java.util.List<Institution> findByStatus(String institutionStatus);
    String selectMaxInstitutionNo();
    String selectMaxInstitutionNoForUpdate();
    int insert(Institution institution);
    int update(Institution institution);
    int deleteById(Long id);
    java.util.List<FeeStatisticsDTO> findFeeStatistics(@Param("region") String region, @Param("year") Integer year, @Param("institutionName") String institutionName, @Param("paymentStatus") String paymentStatus, @Param("offset") int offset, @Param("size") int size);
    Long countFeeStatistics(@Param("region") String region, @Param("year") Integer year, @Param("institutionName") String institutionName, @Param("paymentStatus") String paymentStatus);
}
