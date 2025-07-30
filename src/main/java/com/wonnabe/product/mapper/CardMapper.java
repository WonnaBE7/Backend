package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.dto.UserCardDTO;
import org.apache.ibatis.annotations.Param;

public interface CardMapper {
    /**
     * 카드 ID로 카드 정보를 가져옴
     * @param productId 카드 Id
     * @return 카듯 상품 정보
     */
    CardProductVO findById(long productId);

    /**
     * 사용자가 보유 중인 카드의 정보 조회
     * @param productId 상품 정보
     * @param userId 사용자 Id
     * @return 사용자 카드의 상세 정보
     */
    UserCardVO findUserCardByproductId(
            @Param("productId") long productId,
            @Param("userId") String userId
    ); // 카드 ID와 userID로 사용자 카드 정보를 가져옴

    /**
     * 사용자가 보유중인 카드의 상세 정보 조회
     * @param productId 카드 Id
     * @param userId 사용자 Id
     * @return 사용자가 보유중인 카드의 상세 정보
     */
    UserCardDTO findUserCardDetailById(
            @Param("productId") long productId,
            @Param("userId") String userId
    );

}
