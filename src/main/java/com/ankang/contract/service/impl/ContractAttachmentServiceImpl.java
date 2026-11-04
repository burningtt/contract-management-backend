package com.ankang.contract.service.impl;

import com.ankang.contract.entity.ContractAttachment;
import com.ankang.contract.mapper.ContractAttachmentMapper;
import com.ankang.contract.service.ContractAttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ContractAttachmentServiceImpl implements ContractAttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(ContractAttachmentServiceImpl.class);

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    private static final String EXT_PDF = ".pdf";

    private static final String EXT_JPG = ".jpg";

    private static final String EXT_PNG = ".png";

    private static final String EXT_HEIC = ".heic";

    private final ContractAttachmentMapper attachmentMapper;

    @Value("${file.upload-dir:D:/ankang-uploads}")
    private String uploadDir;

    public ContractAttachmentServiceImpl(ContractAttachmentMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractAttachment uploadAttachment(String contractNo, MultipartFile file, String uploadBy, String remark) {
        if (contractNo == null || contractNo.isEmpty()) {
            throw new IllegalArgumentException("合同编号不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过50MB");
        }

        // 校验并解析文件类型：支持PDF与JPG/PNG/HEIC图片混传
        String extension = resolveExtension(file);
        if (extension == null) {
            throw new IllegalArgumentException("仅支持PDF、JPG、PNG、HEIC格式的文件");
        }

        // 按日期分目录存储：uploadDir/2026-05-18/
        String dateDir = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Path dirPath = Paths.get(uploadDir, dateDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            logger.error("创建上传目录失败: {}", dirPath, e);
            throw new RuntimeException("创建上传目录失败", e);
        }

        // 生成唯一存储文件名，避免文件名冲突
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path filePath = dirPath.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("文件存储失败: {}", filePath, e);
            throw new RuntimeException("文件存储失败", e);
        }

        // 相对路径存入数据库，便于迁移
        String relativePath = dateDir + "/" + storedName;

        ContractAttachment attachment = new ContractAttachment();
        attachment.setContractNo(contractNo);
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(resolveFileType(extension));
        attachment.setUploadBy(uploadBy);
        attachment.setRemark(remark);

        attachmentMapper.insert(attachment);
        logger.info("附件上传成功: contractNo={}, originalName={}, storedName={}",
                contractNo, originalName, storedName);

        return attachment;
    }

    /**
     * 解析上传文件的扩展名
     * @param file 上传文件
     * @return 扩展名（.pdf/.jpg/.png/.heic），不支持时返回null
     */
    private String resolveExtension(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType();
        if (contentType.equals("application/pdf") || name.endsWith(EXT_PDF)) {
            return EXT_PDF;
        }
        if (contentType.equals("image/jpeg") || name.endsWith(EXT_JPG) || name.endsWith(".jpeg")) {
            return EXT_JPG;
        }
        if (contentType.equals("image/png") || name.endsWith(EXT_PNG)) {
            return EXT_PNG;
        }
        if (contentType.equals("image/heic") || name.endsWith(EXT_HEIC)) {
            return EXT_HEIC;
        }
        return null;
    }

    /**
     * 根据扩展名解析文件类型标识
     * @param extension 扩展名
     * @return 文件类型（PDF/JPG/PNG/HEIC）
     */
    private String resolveFileType(String extension) {
        switch (extension) {
            case EXT_PDF:
                return "PDF";
            case EXT_JPG:
                return "JPG";
            case EXT_PNG:
                return "PNG";
            default:
                return "HEIC";
        }
    }

    @Override
    public List<ContractAttachment> findByContractNo(String contractNo) {
        return attachmentMapper.findByContractNo(contractNo);
    }

    @Override
    public ContractAttachment findById(Long id) {
        return attachmentMapper.findById(id);
    }

    @Override
    public Resource downloadAttachment(Long id) {
        ContractAttachment attachment = attachmentMapper.findById(id);
        if (attachment == null) {
            throw new IllegalArgumentException("附件不存在");
        }

        Path filePath = Paths.get(uploadDir, attachment.getFilePath());
        if (!Files.exists(filePath)) {
            logger.error("物理文件不存在: {}", filePath);
            throw new RuntimeException("物理文件不存在");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("文件不可读");
            }
            return resource;
        } catch (MalformedURLException e) {
            logger.error("文件路径转换失败: {}", filePath, e);
            throw new RuntimeException("文件路径转换失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long id) {
        ContractAttachment attachment = attachmentMapper.findById(id);
        if (attachment == null) {
            throw new IllegalArgumentException("附件不存在");
        }

        // 先删除数据库记录
        attachmentMapper.deleteById(id);

        // 再删除物理文件
        Path filePath = Paths.get(uploadDir, attachment.getFilePath());
        try {
            Files.deleteIfExists(filePath);
            logger.info("附件删除成功: id={}, filePath={}", id, filePath);
        } catch (IOException e) {
            logger.warn("物理文件删除失败，但数据库记录已删除: {}", filePath, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByContractNo(String contractNo) {
        List<ContractAttachment> attachments = attachmentMapper.findByContractNo(contractNo);
        attachmentMapper.deleteByContractNo(contractNo);

        for (ContractAttachment attachment : attachments) {
            Path filePath = Paths.get(uploadDir, attachment.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warn("物理文件删除失败: {}", filePath, e);
            }
        }
        logger.info("合同{}的所有附件已删除，共{}个", contractNo, attachments.size());
    }
}
