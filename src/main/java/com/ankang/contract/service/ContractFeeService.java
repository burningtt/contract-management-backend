package com.ankang.contract.service;

import com.ankang.contract.entity.ContractFee;
import com.ankang.contract.mapper.ContractFeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractFeeService {

    private final ContractFeeMapper contractFeeMapper;

    public ContractFeeService(ContractFeeMapper contractFeeMapper) {
        this.contractFeeMapper = contractFeeMapper;
    }

    public ContractFee findById(Long id) {
        return contractFeeMapper.findById(id);
    }

    public List<ContractFee> findByInstitutionNo(String institutionNo) {
        return contractFeeMapper.findByInstitutionNo(institutionNo);
    }

    public List<ContractFee> findAll() {
        return contractFeeMapper.findAll();
    }

    public int create(ContractFee contractFee) {
        return contractFeeMapper.insert(contractFee);
    }

    public int update(ContractFee contractFee) {
        return contractFeeMapper.update(contractFee);
    }

    public int delete(Long id) {
        return contractFeeMapper.deleteById(id);
    }
}
