package com.ankang.contract.mapper;

import com.ankang.contract.entity.WasteHospital;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院Mapper接口
 */
@Mapper
public interface WasteHospitalMapper {

    /**
     * 根据ID查询医院
     * @param id 医院ID
     * @return 医院信息
     */
    WasteHospital findById(Long id);

    /**
     * 根据医院编号查询
     * @param hospitalNo 医院编号
     * @return 医院信息
     */
    WasteHospital findByHospitalNo(String hospitalNo);

    /**
     * 查询所有医院
     * @return 医院列表
     */
    List<WasteHospital> findAll();

    /**
     * 分页查询医院
     * @param offset 偏移量
     * @param size 每页数量
     * @param hospitalName 医院名称（模糊搜索）
     * @param region 地区编码
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 医院列表
     */
    List<WasteHospital> findByPage(@Param("offset") int offset, @Param("size") int size,
                                   @Param("hospitalName") String hospitalName, @Param("region") String region,
                                   @Param("status") Integer status);

    /**
     * 统计医院数量
     * @param hospitalName 医院名称（模糊搜索）
     * @param region 地区编码
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 数量
     */
    Long count(@Param("hospitalName") String hospitalName, @Param("region") String region,
               @Param("status") Integer status);

    /**
     * 查询最大医院编号
     * @return 最大编号
     */
    String selectMaxHospitalNo();

    /**
     * 查询最大医院编号（加锁）
     * @return 最大编号
     */
    String selectMaxHospitalNoForUpdate();

    /**
     * 新增医院
     * @param hospital 医院信息
     * @return 影响行数
     */
    int insert(WasteHospital hospital);

    /**
     * 更新医院
     * @param hospital 医院信息
     * @return 影响行数
     */
    int update(WasteHospital hospital);

    /**
     * 停用医院（逻辑删除）
     * @param id 医院ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 启用医院
     * @param id 医院ID
     * @return 影响行数
     */
    int enableById(Long id);
}
