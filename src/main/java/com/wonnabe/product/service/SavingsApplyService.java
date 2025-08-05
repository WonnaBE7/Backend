package com.wonnabe.product.service;

import com.wonnabe.product.dto.SavingsApplyRequestDTO;

public interface SavingsApplyService {
    /**
     * 예적금 신청
     * @param requestDTO 사용자 예적금 정보
     * @param userId 사용자 Id
     */
    void applyUserSavings(SavingsApplyRequestDTO requestDTO, String userId);
}
