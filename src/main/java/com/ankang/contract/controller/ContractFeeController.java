package com.ankang.contract.controller;

import com.ankang.contract.dto.ApiResponse;
import com.ankang.contract.entity.ContractFee;
import com.ankang.contract.service.ContractFeeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract-fees")
public class ContractFeeController {

    private final ContractFeeService contractFeeService;

    public ContractFeeController(ContractFeeService contractFeeService) {
        this.contractFeeService = contractFeeService;
    }

    @GetMapping("/all")
    public ApiResponse<List<ContractFee>> getAll() {
        return ApiResponse.success(contractFeeService.findAll());
    }

    @GetMapping("/institution/{institutionNo}")
    public ApiResponse<List<ContractFee>> getByInstitutionNo(@PathVariable String institutionNo) {
        return ApiResponse.success(contractFeeService.findByInstitutionNo(institutionNo));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContractFee> getById(@PathVariable Long id) {
        ContractFee contractFee = contractFeeService.findById(id);
        if (contractFee == null) {
            return ApiResponse.error("费用记录不存在");
        }
        return ApiResponse.success(contractFee);
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody ContractFee contractFee) {
        int result = contractFeeService.create(contractFee);
        if (result > 0) {
            return ApiResponse.success("创建成功", null);
        }
        return ApiResponse.error("创建失败");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody ContractFee contractFee) {
        contractFee.setId(id);
        int result = contractFeeService.update(contractFee);
        if (result > 0) {
            return ApiResponse.success("更新成功", null);
        }
        return ApiResponse.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int result = contractFeeService.delete(id);
        if (result > 0) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error("删除失败");
    }
}
