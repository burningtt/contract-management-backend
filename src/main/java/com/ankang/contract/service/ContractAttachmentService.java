package com.ankang.contract.service;

import com.ankang.contract.entity.ContractAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractAttachmentService {

    /**
     * 上传合同附件
     * @param contractNo 合同编号
     * @param file 上传的文件
     * @param uploadBy 上传人
     * @param remark 备注
     * @return 附件信息
     * @throws IllegalArgumentException 当参数不合法时抛出
     * @throws RuntimeException 当文件存储失败时抛出
     */
    ContractAttachment uploadAttachment(String contractNo, MultipartFile file, String uploadBy, String remark);

    /**
     * 根据合同编号查询附件列表
     * @param contractNo 合同编号
     * @return 附件列表
     */
    List<ContractAttachment> findByContractNo(String contractNo);

    /**
     * 根据ID查询附件
     * @param id 附件ID
     * @return 附件信息
     */
    ContractAttachment findById(Long id);

    /**
     * 下载附件
     * @param id 附件ID
     * @return 文件资源
     * @throws IllegalArgumentException 当附件不存在时抛出
     * @throws RuntimeException 当文件读取失败时抛出
     */
    Resource downloadAttachment(Long id);

    /**
     * 删除附件
     * @param id 附件ID
     * @throws IllegalArgumentException 当附件不存在时抛出
     * @throws RuntimeException 当文件删除失败时抛出
     */
    void deleteAttachment(Long id);

    /**
     * 根据合同编号删除所有附件（含物理文件）
     * @param contractNo 合同编号
     */
    void deleteByContractNo(String contractNo);
}
