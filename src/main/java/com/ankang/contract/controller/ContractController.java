package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.dto.ContractCreateDTO;
import com.ankang.contract.dto.ContractQueryDTO;
import com.ankang.contract.dto.PageResponse;
import com.ankang.contract.entity.Contract;
import com.ankang.contract.service.ContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/{id}")
    public ApiResponse<Contract> getById(@PathVariable Long id) {
        Contract contract = contractService.findById(id);
        if (contract == null) {
            return ApiResponse.error("合同不存在");
        }
        return ApiResponse.success(contract);
    }

    @GetMapping
    public ApiResponse<List<Contract>> getAll() {
        return ApiResponse.success(contractService.findAll());
    }

    @GetMapping("/institution/{institutionNo}")
    public ApiResponse<List<Contract>> getByInstitutionNo(@PathVariable String institutionNo) {
        return ApiResponse.success(contractService.findByInstitutionNo(institutionNo));
    }

    @GetMapping("/query")
    public ApiResponse<PageResponse<ContractQueryDTO>> queryContracts(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        PageResponse<ContractQueryDTO> pageResponse = contractService.findInstitutionContractsWithPriority(page, size);
        return ApiResponse.success(pageResponse);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ContractQueryDTO>> searchContracts(
            @RequestParam(required = false) String institutionNo,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        PageResponse<ContractQueryDTO> pageResponse = contractService.searchInstitutionContracts(institutionNo, region, year, keyword, page, size);
        return ApiResponse.success(pageResponse);
    }

    @PostMapping("/create")
    public ApiResponse<Contract> createContract(@RequestBody ContractCreateDTO dto) {
        try {
            logger.info("Creating contract - institutionNo: {}, responsiblePerson: {}", 
                dto.getInstitutionNo(), dto.getResponsiblePerson());
            
            if (dto.getInstitutionNo() == null || dto.getInstitutionNo().isEmpty()) {
                return ApiResponse.error("机构编号不能为空");
            }
            if (dto.getRegion() == null || dto.getRegion().isEmpty()) {
                return ApiResponse.error("地区不能为空");
            }
            if (dto.getSignDate() == null) {
                return ApiResponse.error("签订日期不能为空");
            }
            if (dto.getExpireDate() == null) {
                return ApiResponse.error("到期日期不能为空");
            }
            if (dto.getContractAmount() == null) {
                return ApiResponse.error("合同金额不能为空");
            }
            
            Contract contract = contractService.createContract(dto);
            logger.info("Contract created successfully: {}, responsiblePerson: {}", 
                contract.getContractNo(), contract.getResponsiblePerson());
            return ApiResponse.success("合同创建成功", contract);
        } catch (IllegalArgumentException e) {
            logger.warn("Contract creation failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Contract creation error", e);
            return ApiResponse.error("合同创建失败：" + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody Contract contract) {
        int result = contractService.create(contract);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Contract contract) {
        try {
            contract.setId(id);
            int result = contractService.update(contract);
            if (result > 0) {
                return ApiResponse.success("更新成功", null);
            }
            return ApiResponse.error("更新失败");
        } catch (IllegalArgumentException e) {
            logger.warn("Contract update failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Contract update error", e);
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = contractService.delete(id);
        if (result > 0) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error("删除失败");
    }
}
