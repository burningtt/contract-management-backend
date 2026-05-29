package com.ankang.contract.mapper;

import com.ankang.contract.entity.WasteVehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车辆Mapper接口
 */
@Mapper
public interface WasteVehicleMapper {

    /**
     * 根据ID查询车辆
     * @param id 车辆ID
     * @return 车辆信息
     */
    WasteVehicle findById(Long id);

    /**
     * 根据车辆编号查询
     * @param vehicleNo 车辆编号
     * @return 车辆信息
     */
    WasteVehicle findByVehicleNo(String vehicleNo);

    /**
     * 查询所有车辆
     * @return 车辆列表
     */
    List<WasteVehicle> findAll();

    /**
     * 分页查询车辆
     * @param offset 偏移量
     * @param size 每页数量
     * @param plateNumber 车牌号（模糊搜索）
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 车辆列表
     */
    List<WasteVehicle> findByPage(@Param("offset") int offset, @Param("size") int size,
                                  @Param("plateNumber") String plateNumber, @Param("status") Integer status);

    /**
     * 统计车辆数量
     * @param plateNumber 车牌号（模糊搜索）
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 数量
     */
    Long count(@Param("plateNumber") String plateNumber, @Param("status") Integer status);

    /**
     * 查询最大车辆编号
     * @return 最大编号
     */
    String selectMaxVehicleNo();

    /**
     * 查询最大车辆编号（加锁）
     * @return 最大编号
     */
    String selectMaxVehicleNoForUpdate();

    /**
     * 新增车辆
     * @param vehicle 车辆信息
     * @return 影响行数
     */
    int insert(WasteVehicle vehicle);

    /**
     * 更新车辆
     * @param vehicle 车辆信息
     * @return 影响行数
     */
    int update(WasteVehicle vehicle);

    /**
     * 停用车辆（逻辑删除）
     * @param id 车辆ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 启用车辆
     * @param id 车辆ID
     * @return 影响行数
     */
    int enableById(Long id);
}
