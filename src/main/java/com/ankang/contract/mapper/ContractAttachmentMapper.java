package com.ankang.contract.mapper;

import com.ankang.contract.entity.ContractAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractAttachmentMapper {

    /**
     * 根据ID查询附件
     * @param id 附件ID
     * @return 附件信息
     */
    ContractAttachment findById(@Param("id") Long id);

    /**
     * 根据合同编号查询附件列表
     * @param contractNo 合同编号
     * @return 附件列表
     */
    List<ContractAttachment> findByContractNo(@Param("contractNo") String contractNo);

    /**
     * 统计合同编号下的附件数量
     * @param contractNo 合同编号
     * @return 附件数量
     */
    Long countByContractNo(@Param("contractNo") String contractNo);

    /**
     * 插入附件记录
     * @param attachment 附件信息
     * @return 影响行数
     */
    int insert(ContractAttachment attachment);

    /**
     * 根据ID删除附件
     * @param id 附件ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据合同编号删除所有附件
     * @param contractNo 合同编号
     * @return 影响行数
     */
    int deleteByContractNo(@Param("contractNo") String contractNo);
}
