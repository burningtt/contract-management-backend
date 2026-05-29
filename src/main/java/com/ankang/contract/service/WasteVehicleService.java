package com.ankang.contract.service;

import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.WasteVehicle;
import com.ankang.contract.exception.BusinessException;
import com.ankang.contract.mapper.WasteVehicleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 车辆管理服务
 */
@Service
public class WasteVehicleService {

    private static final Logger logger = LoggerFactory.getLogger(WasteVehicleService.class);
    private static final String INITIAL_NO = "V00001";

    private final WasteVehicleMapper wasteVehicleMapper;

    public WasteVehicleService(WasteVehicleMapper wasteVehicleMapper) {
        this.wasteVehicleMapper = wasteVehicleMapper;
    }

    /**
     * 根据ID查询车辆
     * @param id 车辆ID
     * @return 车辆信息
     */
    public WasteVehicle findById(Long id) {
        return wasteVehicleMapper.findById(id);
    }

    /**
     * 查询所有车辆
     * @return 车辆列表
     */
    public List<WasteVehicle> findAll() {
        return wasteVehicleMapper.findAll();
    }

    /**
     * 分页查询车辆
     * @param page 页码
     * @param size 每页数量
     * @param plateNumber 车牌号（模糊搜索）
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 分页结果
     */
    public PageResponse<WasteVehicle> findByPage(int page, int size, String plateNumber, Integer status) {
        int offset = (page - 1) * size;
        List<WasteVehicle> rows = wasteVehicleMapper.findByPage(offset, size, plateNumber, status);
        Long total = wasteVehicleMapper.count(plateNumber, status);
        return new PageResponse<>(rows, total);
    }

    /**
     * 新增车辆
     * @param vehicle 车辆信息
     * @return 影响行数
     */
    @Transactional
    public int create(WasteVehicle vehicle) {
        String maxNo = wasteVehicleMapper.selectMaxVehicleNoForUpdate();
        String newNo = generateNextNo(maxNo, "V");
        vehicle.setVehicleNo(newNo);
        logger.info("生成车辆编号: {}", newNo);
        return wasteVehicleMapper.insert(vehicle);
    }

    /**
     * 更新车辆
     * @param vehicle 车辆信息
     * @return 影响行数
     */
    public int update(WasteVehicle vehicle) {
        return wasteVehicleMapper.update(vehicle);
    }

    /**
     * 停用车辆（逻辑删除）
     * @param id 车辆ID
     * @return 影响行数
     */
    public int delete(Long id) {
        return wasteVehicleMapper.deleteById(id);
    }

    /**
     * 启用车辆
     * @param id 车辆ID
     * @return 影响行数
     */
    public int enable(Long id) {
        return wasteVehicleMapper.enableById(id);
    }

    /**
     * 生成下一个编号
     * @param maxNo 当前最大编号
     * @param prefix 编号前缀
     * @return 新编号
     */
    private String generateNextNo(String maxNo, String prefix) {
        if (maxNo == null || maxNo.isEmpty()) {
            return prefix + "00001";
        }
        try {
            // 提取前缀后的数字部分
            String numPart = maxNo.substring(prefix.length());
            long num = Long.parseLong(numPart) + 1;
            return prefix + String.format("%05d", num);
        } catch (NumberFormatException e) {
            logger.error("编号格式错误: {}", maxNo);
            throw new BusinessException("编号生成失败：编号格式错误");
        }
    }
}
