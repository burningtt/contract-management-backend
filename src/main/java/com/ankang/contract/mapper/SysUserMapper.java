package com.ankang.contract.mapper;

import com.ankang.contract.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser findByUsername(String username);
    SysUser findById(Long id);
    List<SysUser> findAll();
    int insert(SysUser user);
    int update(SysUser user);
    int deleteById(Long id);
}
