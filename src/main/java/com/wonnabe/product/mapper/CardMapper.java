package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.dto.UserCardDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

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

    /**
     * 사용자가 등록한 카드 등록
     * @param userCard 사용자가 등록한 카드 정보
     */
    void insertUserCard(UserCardVO userCard);

    /**
     * 사용자가 보유한 카드 목록 정보 최신화
     * @param cardId 등록한 사용자 카드 아이디
     * @param userId 사용자 아이디
     */
    void updateUserCardInfo(
            @Param("cardId") long cardId,
            @Param("userId") String userId
    );

    /**
     * 사용자가 보유한 계좌 고유 Id
     * @param accountNumber 계좌번호
     * @param userId 사용자 아이디
     * @return 사용자 계좌 고유 아이디 반환
     */
    Long getAccountId(
            @Param("accountNumber") String accountNumber,
            @Param("userId") String userId
    );

    /**
     * 내가 보유한 카드 목록 조회
     * @param userId 사용자 아이디
     * @return 카드 목록 리스
     */
    String getMyCardIdsJson(@Param("userId") String userId);

    /**
     * 최근에 입력된 카드의 숫자를 가져옴
     * @return 최근에 입력된 카드의 숫자
     */
    String findLastCardNumber();
}
