package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.UploadSession;
import com.ankang.contract.entity.ContractAttachment;
import com.ankang.contract.service.ContractAttachmentService;
import com.ankang.contract.service.UploadSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/attachments")
public class ContractAttachmentController {

    private static final Logger logger = LoggerFactory.getLogger(ContractAttachmentController.class);

    private final ContractAttachmentService attachmentService;

    private final UploadSessionService uploadSessionService;

    public ContractAttachmentController(ContractAttachmentService attachmentService,
                                       UploadSessionService uploadSessionService) {
        this.attachmentService = attachmentService;
        this.uploadSessionService = uploadSessionService;
    }

    /**
     * 上传合同附件
     * @param contractNo 合同编号
     * @param file 上传的PDF文件
     * @param uploadBy 上传人
     * @param remark 备注
     * @return 上传成功的附件信息
     */
    @PostMapping("/upload")
    public ApiResponse<ContractAttachment> upload(
            @RequestParam("contractNo") String contractNo,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadBy", required = false) String uploadBy,
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            ContractAttachment attachment = attachmentService.uploadAttachment(contractNo, file, uploadBy, remark);
            return ApiResponse.success("上传成功", attachment);
        } catch (IllegalArgumentException e) {
            logger.warn("附件上传参数错误: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("附件上传失败", e);
            return ApiResponse.error("附件上传失败：" + e.getMessage());
        }
    }

    /**
     * 根据合同编号查询附件列表
     * @param contractNo 合同编号
     * @return 附件列表
     */
    @GetMapping("/list/{contractNo}")
    public ApiResponse<List<ContractAttachment>> listByContractNo(@PathVariable String contractNo) {
        List<ContractAttachment> attachments = attachmentService.findByContractNo(contractNo);
        return ApiResponse.success(attachments);
    }

    /**
     * 根据ID查询附件详情
     * @param id 附件ID
     * @return 附件信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ContractAttachment> getById(@PathVariable Long id) {
        ContractAttachment attachment = attachmentService.findById(id);
        if (attachment == null) {
            return ApiResponse.error("附件不存在");
        }
        return ApiResponse.success(attachment);
    }

    /**
     * 下载附件
     * @param id 附件ID
     * @return 文件资源响应
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            ContractAttachment attachment = attachmentService.findById(id);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = attachmentService.downloadAttachment(id);

            // 对文件名进行URL编码，解决中文文件名下载乱码问题
            String encodedFileName = URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(resolveMediaType(attachment))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warn("附件下载参数错误: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            logger.error("附件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 在线预览附件（以inline方式打开PDF）
     * @param id 附件ID
     * @return 文件资源响应
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        try {
            ContractAttachment attachment = attachmentService.findById(id);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = attachmentService.downloadAttachment(id);

            return ResponseEntity.ok()
                    .contentType(resolveMediaType(attachment))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warn("附件预览参数错误: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            logger.error("附件预览失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建手机扫码上传会话
     * @param contractNo 合同编号
     * @param uploadBy 上传人（当前登录用户）
     * @return 上传会话信息（含短时效token）
     */
    @PostMapping("/upload-session")
    public ApiResponse<UploadSession> createUploadSession(
            @RequestParam("contractNo") String contractNo,
            @RequestParam(value = "uploadBy", required = false) String uploadBy) {
        if (contractNo == null || contractNo.isEmpty()) {
            return ApiResponse.error("合同编号不能为空");
        }
        return ApiResponse.success("创建成功", uploadSessionService.createSession(contractNo, uploadBy));
    }

    /**
     * 根据附件文件类型解析HTTP Content-Type
     * @param attachment 附件信息
     * @return 对应的MediaType
     */
    private MediaType resolveMediaType(ContractAttachment attachment) {
        switch (attachment.getFileType() == null ? "PDF" : attachment.getFileType()) {
            case "JPG":
                return MediaType.IMAGE_JPEG;
            case "PNG":
                return MediaType.IMAGE_PNG;
            case "HEIC":
                return MediaType.parseMediaType("image/heic");
            default:
                return MediaType.APPLICATION_PDF;
        }
    }

    /**
     * 删除附件
     * @param id 附件ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        try {
            attachmentService.deleteAttachment(id);
            return ApiResponse.success("删除成功", null);
        } catch (IllegalArgumentException e) {
            logger.warn("附件删除参数错误: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("附件删除失败", e);
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }
}
