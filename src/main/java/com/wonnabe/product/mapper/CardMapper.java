package com.wonnabe.product.mapper;

import java.util.List;

import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.BasicUserInfo;
import com.wonnabe.product.dto.UserCardDTO;
import com.wonnabe.product.dto.UserInfoForCardDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
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

    /**
     * 카드 추천을 위한 사용자 정보 조회
     * @param userId 사용자 id
     * @return 추천에 필요한 사용자 정보
     */
    UserInfoForCardDTO findUserInfoForCardRecommend(String userId);

    /**
     * 모든 카드 상품 조회
     * @return 카드 상품 리스트
     */
    List<CardProductVO> findAllCardProducts();

    /**
     * 사용자가 보유 중인 카드 상품 번호 조회
     * @param userCardIds 사용자 카드 아이디
     * @return 사용자 카드 상품 번호
     */
    List<Long> findProductIdsByUserCardIds(@Param("userCardIds") List<Long> userCardIds);

    /**
     * 사용자가 보유 중인 카드 상품 번호 조회
     * @param userId 사용자 아이디
     * @return 사용자가 보유한 카드 상품 번호
     */
    List<Long> findProductIdsByUserId(@Param("userId") String userId);

    /**
     * 카드 상품 목록 조회
     * @param productIds 조회할 상품 아이디들
     * @return 카등 상품 목록
     */
    List<CardProductVO> findProductsByIds(@Param("productIds") List<Long> productIds);

    /**
     * 카드 추천 상품의 상세 정보 조회를 위한 사용자 정보
     * @param userId 사용자 아이디
     * @return 사용자 기본 정보
     */
    BasicUserInfo findBasicUserInfoById(@Param("userId") String userId);

}
