package com.ankang.contract.service;

import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.WasteStaff;
import com.ankang.contract.exception.BusinessException;
import com.ankang.contract.mapper.WasteStaffMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 车队人员管理服务
 */
@Service
public class WasteStaffService {

    private static final Logger logger = LoggerFactory.getLogger(WasteStaffService.class);

    private final WasteStaffMapper wasteStaffMapper;

    public WasteStaffService(WasteStaffMapper wasteStaffMapper) {
        this.wasteStaffMapper = wasteStaffMapper;
    }

    /**
     * 根据ID查询人员
     * @param id 人员ID
     * @return 人员信息
     */
    public WasteStaff findById(Long id) {
        return wasteStaffMapper.findById(id);
    }

    /**
     * 查询所有人员
     * @return 人员列表
     */
    public List<WasteStaff> findAll() {
        return wasteStaffMapper.findAll();
    }

    /**
     * 分页查询人员
     * @param page 页码
     * @param size 每页数量
     * @param staffName 姓名（模糊搜索）
     * @param position 岗位
     * @param status 状态（1-正常，0-已停用，null-全部）
     * @return 分页结果
     */
    public PageResponse<WasteStaff> findByPage(int page, int size, String staffName, String position, Integer status) {
        int offset = (page - 1) * size;
        List<WasteStaff> rows = wasteStaffMapper.findByPage(offset, size, staffName, position, status);
        Long total = wasteStaffMapper.count(staffName, position, status);
        return new PageResponse<>(rows, total);
    }

    /**
     * 新增人员
     * @param staff 人员信息
     * @return 影响行数
     */
    @Transactional
    public int create(WasteStaff staff) {
        String maxNo = wasteStaffMapper.selectMaxStaffNoForUpdate();
        String newNo = generateNextNo(maxNo, "S");
        staff.setStaffNo(newNo);
        logger.info("生成人员编号: {}", newNo);
        return wasteStaffMapper.insert(staff);
    }

    /**
     * 更新人员
     * @param staff 人员信息
     * @return 影响行数
     */
    public int update(WasteStaff staff) {
        return wasteStaffMapper.update(staff);
    }

    /**
     * 停用人员（逻辑删除）
     * @param id 人员ID
     * @return 影响行数
     */
    public int delete(Long id) {
        return wasteStaffMapper.deleteById(id);
    }

    /**
     * 启用人员
     * @param id 人员ID
     * @return 影响行数
     */
    public int enable(Long id) {
        return wasteStaffMapper.enableById(id);
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
            String numPart = maxNo.substring(prefix.length());
            long num = Long.parseLong(numPart) + 1;
            return prefix + String.format("%05d", num);
        } catch (NumberFormatException e) {
            logger.error("编号格式错误: {}", maxNo);
            throw new BusinessException("编号生成失败：编号格式错误");
        }
    }
}
