package com.wonnabe.product.mapper;

import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
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
}
