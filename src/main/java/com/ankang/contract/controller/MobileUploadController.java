package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.UploadSession;
import com.ankang.contract.entity.ContractAttachment;
import com.ankang.contract.service.ContractAttachmentService;
import com.ankang.contract.service.UploadSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手机扫码免登录上传接口
 * 通过PC端生成的短时效会话token鉴权，token绑定合同号
 */
@RestController
@RequestMapping("/mobile-upload")
public class MobileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(MobileUploadController.class);

    private final UploadSessionService uploadSessionService;

    private final ContractAttachmentService attachmentService;

    public MobileUploadController(UploadSessionService uploadSessionService,
                                  ContractAttachmentService attachmentService) {
        this.uploadSessionService = uploadSessionService;
        this.attachmentService = attachmentService;
    }

    /**
     * 查询会话信息（手机端页面加载时调用）
     * @param token 会话token
     * @return 合同号、上传人、有效期与已上传附件数
     */
    @GetMapping("/{token}/info")
    public ApiResponse<Map<String, Object>> info(@PathVariable String token) {
        UploadSession session = uploadSessionService.getSession(token);
        if (session == null) {
            return ApiResponse.error("上传会话不存在或已过期，请在电脑端重新生成二维码");
        }
        List<ContractAttachment> attachments = attachmentService.findByContractNo(session.getContractNo());
        Map<String, Object> data = new HashMap<>();
        data.put("contractNo", session.getContractNo());
        data.put("uploadBy", session.getUploadBy());
        data.put("expireTime", session.getExpireTime());
        data.put("uploadedCount", attachments.size());
        return ApiResponse.success(data);
    }

    /**
     * 手机端上传附件（免登录，凭会话token鉴权）
     * @param token 会话token
     * @param file 上传的文件（图片或PDF）
     * @return 上传成功的附件信息
     */
    @PostMapping("/{token}")
    public ApiResponse<ContractAttachment> upload(@PathVariable String token,
                                                  @RequestParam("file") MultipartFile file) {
        UploadSession session = uploadSessionService.getSession(token);
        if (session == null) {
            return ApiResponse.error("上传会话不存在或已过期，请在电脑端重新生成二维码");
        }
        try {
            // 上传人沿用PC端会话创建人，备注标记来源便于区分
            ContractAttachment attachment = attachmentService.uploadAttachment(
                    session.getContractNo(), file, session.getUploadBy(), "手机扫码上传");
            logger.info("手机扫码上传成功: contractNo={}, fileName={}", session.getContractNo(), attachment.getOriginalName());
            return ApiResponse.success("上传成功", attachment);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("手机扫码上传失败: token={}", token, e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }
}
