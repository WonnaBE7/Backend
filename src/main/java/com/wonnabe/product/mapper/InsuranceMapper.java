package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InsuranceMapper {

    /**
     * 보험 ID로 보험 상품 정보를 가져옴
     * @param productId 보험 Id
     * @return 보험 상품 정보
     */
    InsuranceProductVO findById(@Param("productId") long productId);

    /**
     * 사용자가 가입한 보험 등록
     * @param userInsurance 사용자가 가입한 보험 정보
     */
    void insertUserInsurance(UserInsuranceVO userInsurance);

    /**
     * 사용자 보유 보험 목록 최신화
     * @param insuranceId 등록한 사용자 보험 아이디
     * @param userId 사용자 아이디
     */
    void updateUserInsuranceInfo(@Param("insuranceId") long insuranceId, @Param("userId") String userId);

    /**
     * 사용자가 등록한 보험 정보 조회
     * @param productId 상품 정보
     * @param userId 사용자 Id
     * @return 사용자 보험 정보
     */
    UserInsuranceVO findUserInsuranceByProductId(@Param("productId") long productId, @Param("userId") String userId);

    /**
     * 내가 보유한 보험 목록 조회
     * @param userId 사용자 아이디
     * @return 보험 목록 JSON
     */
    String getMyInsuranceIdsJson(@Param("userId") String userId);

    /**
     * userId로 사용자의 성별을 조회
     * @param userId 사용자 아이디
     * @return 사용자 성별 (F/M)
     */
    String findGenderByUserId(@Param("userId") String userId);
}