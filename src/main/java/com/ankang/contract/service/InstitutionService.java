package com.ankang.contract.service;

import com.ankang.contract.dto.FeeStatisticsDTO;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.Institution;
import com.ankang.contract.exception.BusinessException;
import com.ankang.contract.mapper.InstitutionMapper;
import com.ankang.contract.mapper.InstitutionNoGenerator;
import com.ankang.contract.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class InstitutionService {

    private static final Logger logger = LoggerFactory.getLogger(InstitutionService.class);
    private static final Pattern INSTITUTION_NO_PATTERN = Pattern.compile("^\\d{6}$");
    private static final String INITIAL_NO = "100001";

    private final InstitutionMapper institutionMapper;
    private final InstitutionNoGenerator institutionNoGenerator;

    public InstitutionService(InstitutionMapper institutionMapper, InstitutionNoGenerator institutionNoGenerator) {
        this.institutionMapper = institutionMapper;
        this.institutionNoGenerator = institutionNoGenerator;
    }

    public Institution findById(Long id) {
        return institutionMapper.findById(id);
    }

    public Institution findByInstitutionNo(String institutionNo) {
        return institutionMapper.findByInstitutionNo(institutionNo);
    }

    public List<Institution> findAll() {
        return institutionMapper.findAll();
    }

    public PageResponse<Institution> findByPage(int page, int size, String institutionName, String institutionType, String institutionStatus, String region) {
        int offset = (page - 1) * size;
        List<Institution> rows = institutionMapper.findByPage(offset, size, institutionName, institutionType, institutionStatus, region);
        Long total = institutionMapper.count(institutionName, institutionType, institutionStatus, region);
        return new PageResponse<>(rows, total);
    }

    public List<Institution> findByType(String institutionType) {
        return institutionMapper.findByType(institutionType);
    }

    public List<Institution> findByStatus(String institutionStatus) {
        return institutionMapper.findByStatus(institutionStatus);
    }

    public PageResponse<FeeStatisticsDTO> findFeeStatistics(String region, Integer year, String institutionName, String paymentStatus, int page, int size) {
        int offset = (page - 1) * size;
        List<FeeStatisticsDTO> rows = institutionMapper.findFeeStatistics(region, year, institutionName, paymentStatus, offset, size);
        Long total = institutionMapper.countFeeStatistics(region, year, institutionName, paymentStatus);
        return new PageResponse<>(rows, total);
    }

    @Transactional
    public int create(Institution institution) {
        String maxNo = institutionNoGenerator.getNextInstitutionNo();
        String newNo;
        
        if (maxNo == null || maxNo.isEmpty()) {
            newNo = INITIAL_NO;
        } else {
            try {
                long no = Long.parseLong(maxNo) + 1;
                if (no < 100001) {
                    no = 100001;
                }
                newNo = String.format("%06d", no);
            } catch (NumberFormatException e) {
                logger.error("机构编号格式错误: {}", maxNo);
                throw new BusinessException("机构编号生成失败：编号格式错误");
            }
        }
        
        if (!validateInstitutionNo(newNo)) {
            throw new BusinessException("机构编号验证失败：编号必须为6位数字且从100001开始");
        }
        
        if (institutionMapper.findByInstitutionNo(newNo) != null) {
            throw new BusinessException("机构编号已存在，请重试");
        }
        
        institution.setInstitutionNo(newNo);
        String createdBy = institution.getCreatedBy();
        if (createdBy == null || createdBy.isEmpty()) {
            String realName = SecurityUtils.getCurrentRealName();
            createdBy = realName != null ? realName : "系统";
        }
        institution.setCreatedBy(createdBy);
        logger.info("成功生成机构编号: {}, 录入人: {}", newNo, createdBy);
        return institutionMapper.insert(institution);
    }

    private boolean validateInstitutionNo(String institutionNo) {
        if (institutionNo == null || !INSTITUTION_NO_PATTERN.matcher(institutionNo).matches()) {
            return false;
        }
        long no = Long.parseLong(institutionNo);
        return no >= 100001;
    }

    public int update(Institution institution) {
        return institutionMapper.update(institution);
    }

    public int delete(Long id) {
        return institutionMapper.deleteById(id);
    }
}
