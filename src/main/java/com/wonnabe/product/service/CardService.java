package com.wonnabe.product.service;

import com.wonnabe.product.dto.UserCardDetailDTO;

public interface CardService {
    // 유저 카드의 상세 정보를 반환하는 함수
    UserCardDetailDTO findUserCardDetail(long productId, String userId);
}
