package com.wonnabe.product.mapper;

import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.dto.MonthlyInsuranceReceiptDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.sql.Date;

/**
 * 사용자의 보유 보험 상품 정보에 접근하는 MyBatis Mapper 인터페이스.
 */
@Mapper
public interface UserInsuranceMapper {

    /**
     * 사용자의 특정 보유 보험 상품 상세 정보를 조회합니다.
     *
     * @param userId 사용자의 고유 ID
     * @param productId 조회할 사용자의 보험 가입 ID
     * @return {@link UserInsuranceVO} 객체. 가입 정보와 상품 상세 정보를 포함합니다.
     */
    UserInsuranceVO findDetailByProductId(@Param("userId") String userId,
                                          @Param("productId") Long productId);

    /**
     * 특정 사용자의 보험 관련 거래 내역을 월별로 합산하여 조회합니다.
     *
     * @param userId 사용자의 고유 ID
     * @param startDate 조회 시작 날짜 (YYYY-MM-DD 형식)
     * @return 월별 거래 합계 목록
     */
    List<TransactionSummaryDto> findMonthlyTransactionSums(@Param("userId") String userId,
                                                           @Param("startDate") Date startDate);

    List<UserInsuranceVO> findAllByUserId(String userId);

    /**
     * 특정 상품 ID로 보험 상품 상세 정보를 조회합니다.
     * @param productId 조회할 상품의 고유 ID
     * @return {@link InsuranceProductVO} 객체
     */
    InsuranceProductVO findInsuranceProductById(@Param("productId") Long productId);

    /**
     * 특정 사용자의 특정 보험 상품에 대한 총 수령액(입금)을 조회합니다.
     * User_Transactions 테이블에서 asset_category='보험', transaction_type='입금'인 거래의 합계.
     * @param userId 사용자의 고유 ID
     * @param productId 보험 상품 ID
     * @return 총 수령액
     */
    Long findTotalReceiptAmount(@Param("userId") String userId, @Param("productId") Long productId);

    /**
     * 특정 사용자의 특정 보험 상품에 대한 총 납입액(출금)을 조회합니다.
     * User_Transactions 테이블에서 asset_category='보험', transaction_type='출금'인 거래의 합계.
     * @param userId 사용자의 고유 ID
     * @param productId 보험 상품 ID
     * @return 총 납입액
     */
    Long findTotalPaymentAmount(@Param("userId") String userId, @Param("productId") Long productId);

    /**
     * 특정 사용자의 특정 보험 상품에 대한 월별 수령액(입금)을 조회합니다.
     * @param userId 사용자의 고유 ID
     * @param startDate 조회 시작 날짜
     * @return 월별 수령액 목록
     */
    List<MonthlyInsuranceReceiptDto> findMonthlyInsuranceReceipts(@Param("userId") String userId, @Param("productId") Long productId, @Param("startDate") Date startDate);
}
