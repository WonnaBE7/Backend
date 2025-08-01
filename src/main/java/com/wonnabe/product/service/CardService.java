package com.wonnabe.product.service;

import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import org.springframework.transaction.annotation.Transactional;

public interface CardService {
    // 유저 카드의 상세 정보를 반환하는 함수

    /**
     * 사용자가 보유한 특정 카드의 상세 정보 조회
     * @param productId 카드 Id
     * @param userId 사용자 Id
     * @return
     */
    UserCardDetailDTO findUserCardDetail(long productId, String userId);

    /**
     * 카드 신청
     * @param cardApplyRequestDTO 사용자 카드 정보
     * @param userId 사용자 Id
     * @throws Exception 사용자 카드를 삽입 후 id를 가져오지 못하면 예외 처리
     */
    void applyUserCard(CardApplyRequestDTO cardApplyRequestDTO, String userId);
}
