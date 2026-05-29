package com.ankang.contract.mapper;

import com.ankang.contract.entity.WasteStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车队人员Mapper接口
 */
@Mapper
public interface WasteStaffMapper {

    /**
     * 根据ID查询人员
     * @param id 人员ID
     * @return 人员信息
     */
    WasteStaff findById(Long id);

    /**
     * 根据人员编号查询
     * @param staffNo 人员编号
     * @return 人员信息
     */
    WasteStaff findByStaffNo(String staffNo);

    /**
     * 查询所有人员
     * @return 人员列表
     */
    List<WasteStaff> findAll();

    /**
     * 分页查询人员
     * @param offset 偏移量
     * @param size 每页数量
     * @param staffName 姓名（模糊搜索）
     * @param position 岗位
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 人员列表
     */
    List<WasteStaff> findByPage(@Param("offset") int offset, @Param("size") int size,
                                @Param("staffName") String staffName, @Param("position") String position,
                                @Param("status") Integer status);

    /**
     * 统计人员数量
     * @param staffName 姓名（模糊搜索）
     * @param position 岗位
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 数量
     */
    Long count(@Param("staffName") String staffName, @Param("position") String position,
               @Param("status") Integer status);

    /**
     * 查询最大人员编号
     * @return 最大编号
     */
    String selectMaxStaffNo();

    /**
     * 查询最大人员编号（加锁）
     * @return 最大编号
     */
    String selectMaxStaffNoForUpdate();

    /**
     * 新增人员
     * @param staff 人员信息
     * @return 影响行数
     */
    int insert(WasteStaff staff);

    /**
     * 更新人员
     * @param staff 人员信息
     * @return 影响行数
     */
    int update(WasteStaff staff);

    /**
     * 停用人员（逻辑删除）
     * @param id 人员ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 启用人员
     * @param id 人员ID
     * @return 影响行数
     */
    int enableById(Long id);
}
