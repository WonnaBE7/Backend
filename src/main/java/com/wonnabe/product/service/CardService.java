package com.wonnabe.product.service;

import com.wonnabe.product.dto.UserCardDetailDTO;

public interface CardService {
    // 유저 카드의 상세 정보를 반환하는 함수

    /**
     * 사용자가 보유한 특정 카드의 상세 정보 조회
     * @param productId 카드 Id
     * @param userId 사용자 Id
     * @return
     */
    UserCardDetailDTO findUserCardDetail(long productId, String userId);
}
