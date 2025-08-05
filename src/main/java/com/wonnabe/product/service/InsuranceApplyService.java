package com.wonnabe.product.service;

import com.wonnabe.product.dto.InsuranceApplyRequestDTO;

public interface InsuranceApplyService {
    /**
     * 보험 신청
     * @param insuranceApplyRequestDTO 사용자 보험 정보
     * @param userId 사용자 Id
     */
    void applyUserInsurance(InsuranceApplyRequestDTO insuranceApplyRequestDTO, String userId);
}
