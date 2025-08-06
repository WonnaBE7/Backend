package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SavingsMapper {

    /**
     * 예적금 ID로 상품 정보를 가져옴
     * @param productId 상품 Id
     * @return 예적금 상품 정보
     */
    SavingsProductVO findById(@Param("productId") long productId);

    /**
     * 사용자가 가입한 예적금 등록
     * @param userSavings 사용자가 가입한 예적금 정보
     */
    void insertUserSavings(UserSavingsVO userSavings);

    /**
     * 사용자 보유 예적금 목록 최신화
     * @param savingsId 등록한 사용자 예적금 아이디
     * @param userId 사용자 아이디
     */
    void updateUserSavingsInfo(@Param("savingsId") long savingsId, @Param("userId") String userId);

    /**
     * 사용자가 등록한 예적금 정보 조회
     * @param productId 상품 정보
     * @param userId 사용자 Id
     * @return 사용자 예적금 정보 (가장 최근 1건)
     */
    UserSavingsVO findUserSavingsByProductId(@Param("productId") long productId, @Param("userId") String userId);

    /**
     * 내가 보유한 예적금 목록 조회
     * @param userId 사용자 아이디
     * @return 예적금 목록 JSON
     */
    String getMySavingsIdsJson(@Param("userId") String userId);
}
