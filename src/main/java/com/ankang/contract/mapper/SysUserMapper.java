package com.ankang.contract.mapper;

import com.ankang.contract.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper {
    SysUser findByUsername(String username);
    SysUser findById(Long id);
    int insert(SysUser user);
    int update(SysUser user);
}
