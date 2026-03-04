package com.ankang.contract.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstitutionNoGenerator {
    String getNextInstitutionNo();
}
