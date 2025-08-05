package com.wonnabe.product.service;

import com.wonnabe.product.dto.UserInsuranceDetailDTO;

/**
 * 사용자의 보유 보험 상품 관련 비즈니스 로직을 처리하는 서비스 인터페이스.
 */
public interface UserInsuranceService {

    /**
     * 특정 보유 보험 상품의 상세 정보를 조회합니다.
     *
     * @param userId 사용자의 고유 ID
     * @param productId 조회할 보험 상품의 고유 ID
     * @return {@link UserInsuranceDetailDTO} API 응답에 맞게 가공된 데이터. 없으면 null 반환.
     */
    UserInsuranceDetailDTO getDetailByProductId(String userId, Long productId);

    String existsById(Long productId);
}

