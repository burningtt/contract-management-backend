package com.ankang.contract.mapper;

import com.ankang.contract.entity.WasteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 医废记录Mapper接口
 */
@Mapper
public interface WasteRecordMapper {

    /**
     * 根据ID查询记录
     * @param id 记录ID
     * @return 记录信息（含关联名称）
     */
    WasteRecord findById(Long id);

    /**
     * 分页查询记录（含关联名称）
     * @param offset 偏移量
     * @param size 每页数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称（模糊搜索）
     * @param batchNo 批次编号
     * @return 记录列表
     */
    List<WasteRecord> findByPage(@Param("offset") int offset,
                                  @Param("size") int size,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("region") String region,
                                  @Param("hospitalName") String hospitalName,
                                  @Param("batchNo") String batchNo);

    /**
     * 统计记录数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称（模糊搜索）
     * @param batchNo 批次编号
     * @return 数量
     */
    Long count(@Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate,
               @Param("region") String region,
               @Param("hospitalName") String hospitalName,
               @Param("batchNo") String batchNo);

    /**
     * 查询指定医院最近一条记录
     * @param hospitalNo 医院编号
     * @return 最近一条记录
     */
    WasteRecord findLastByHospitalNo(String hospitalNo);

    /**
     * 查询指定日期的最大批次序号
     * @param datePrefix 日期前缀（如YF20260522）
     * @return 最大批次编号
     */
    String selectMaxBatchNoByDatePrefix(String datePrefix);

    /**
     * 查询指定日期的最大批次序号（加锁）
     * @param datePrefix 日期前缀
     * @return 最大批次编号
     */
    String selectMaxBatchNoByDatePrefixForUpdate(String datePrefix);

    /**
     * 新增记录
     * @param record 记录信息
     * @return 影响行数
     */
    int insert(WasteRecord record);

    /**
     * 更新记录
     * @param record 记录信息
     * @return 影响行数
     */
    int update(WasteRecord record);

    /**
     * 查询全部符合条件的记录（不分页，用于导出）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param region 地区编码
     * @param hospitalName 医院名称（模糊搜索）
     * @param batchNo 批次编号
     * @return 记录列表
     */
    List<WasteRecord> findAllForExport(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("region") String region,
                                       @Param("hospitalName") String hospitalName,
                                       @Param("batchNo") String batchNo);

    /**
     * 删除记录
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
