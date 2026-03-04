package com.ankang.contract.service;

import com.ankang.contract.dto.ContractCreateDTO;
import com.ankang.contract.dto.ContractQueryDTO;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.Contract;
import com.ankang.contract.mapper.ContractMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
public class ContractService {

    private static final Logger logger = LoggerFactory.getLogger(ContractService.class);
    private final ContractMapper contractMapper;

    public ContractService(ContractMapper contractMapper) {
        this.contractMapper = contractMapper;
    }

    public Contract findById(Long id) {
        return contractMapper.findById(id);
    }

    public List<Contract> findAll() {
        return contractMapper.findAll();
    }

    public List<Contract> findByInstitutionNo(String institutionNo) {
        return contractMapper.findByInstitutionNo(institutionNo);
    }

    public PageResponse<ContractQueryDTO> findInstitutionContractsWithPriority(int page, int size) {
        int offset = (page - 1) * size;
        List<ContractQueryDTO> rows = contractMapper.findInstitutionContractsWithPriority(offset, size);
        Long total = contractMapper.countInstitutionContractsWithPriority();
        return new PageResponse<>(rows, total);
    }

    public PageResponse<ContractQueryDTO> searchInstitutionContracts(String institutionNo, String region, Integer year, String keyword, int page, int size) {
        int offset = (page - 1) * size;
        List<ContractQueryDTO> rows = contractMapper.searchInstitutionContracts(institutionNo, region, year, keyword, offset, size);
        Long total = contractMapper.countSearchInstitutionContracts(institutionNo, region, year, keyword);
        return new PageResponse<>(rows, total);
    }

    public Contract createContract(ContractCreateDTO dto) {
        logger.info("createContract called - institutionNo: {}, responsiblePerson: {}", 
            dto.getInstitutionNo(), dto.getResponsiblePerson());
        
        validateContractDates(dto.getInstitutionNo(), null, dto.getSignDate(), dto.getExpireDate());
        
        String contractNo = generateContractNo(dto.getRegion());
        String status = determineContractStatus(dto.getSignDate(), dto.getExpireDate());
        
        Contract contract = new Contract();
        contract.setContractNo(contractNo);
        contract.setInstitutionNo(dto.getInstitutionNo());
        contract.setContractAmount(dto.getContractAmount());
        contract.setSignDate(dto.getSignDate());
        contract.setExpireDate(dto.getExpireDate());
        contract.setResponsiblePerson(dto.getResponsiblePerson());
        contract.setContractStatus(status);
        contract.setRemark(dto.getRemark());
        
        logger.info("Before insert - contractNo: {}, responsiblePerson: {}", 
            contract.getContractNo(), contract.getResponsiblePerson());
        
        contractMapper.insert(contract);
        logger.info("Created contract: {} for institution: {}, responsiblePerson: {}", 
            contractNo, dto.getInstitutionNo(), contract.getResponsiblePerson());
        
        return contract;
    }

    String generateContractNo(String region) {
        int year = Year.now().getValue();
        String maxContractNo = contractMapper.findMaxContractNoByYearAndRegion(year, region);
        
        int sequence = 1;
        if (maxContractNo != null && maxContractNo.length() >= 11) {
            String sequenceStr = maxContractNo.substring(7);
            try {
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse sequence from contract no: {}", maxContractNo);
                sequence = 1;
            }
        }
        
        return String.format("%d%s%04d", year, region, sequence);
    }

    String determineContractStatus(LocalDate signDate, LocalDate expireDate) {
        LocalDate today = LocalDate.now();
        
        if (signDate != null && signDate.isAfter(today)) {
            return "PENDING";
        }
        if (expireDate != null && expireDate.isBefore(today)) {
            return "INACTIVE";
        }
        return "ACTIVE";
    }

    void validateContractDates(String institutionNo, Long excludeId, LocalDate signDate, LocalDate expireDate) {
        if (signDate != null && expireDate != null && signDate.isAfter(expireDate)) {
            throw new IllegalArgumentException("签订日期不能晚于到期日期");
        }
        
        if (signDate == null || expireDate == null) {
            return;
        }
        
        List<Contract> existingContracts = contractMapper.findByInstitutionNoForDateCheck(institutionNo, excludeId);
        
        for (Contract existing : existingContracts) {
            if (existing.getSignDate() == null || existing.getExpireDate() == null) {
                continue;
            }
            
            boolean hasOverlap = !(expireDate.isBefore(existing.getSignDate()) || signDate.isAfter(existing.getExpireDate()));
            
            if (hasOverlap) {
                throw new IllegalArgumentException(
                    String.format("合同日期与现有合同[%s]存在重叠（%s ~ %s）", 
                        existing.getContractNo(), 
                        existing.getSignDate(), 
                        existing.getExpireDate())
                );
            }
        }
    }

    public int create(Contract contract) {
        return contractMapper.insert(contract);
    }

    public int update(Contract contract) {
        Contract existingContract = contractMapper.findById(contract.getId());
        if (existingContract == null) {
            throw new IllegalArgumentException("合同不存在");
        }
        
        validateContractDates(existingContract.getInstitutionNo(), contract.getId(), contract.getSignDate(), contract.getExpireDate());
        
        String status = determineContractStatus(contract.getSignDate(), contract.getExpireDate());
        contract.setContractStatus(status);
        
        return contractMapper.update(contract);
    }

    public int delete(Long id) {
        return contractMapper.deleteById(id);
    }
}
