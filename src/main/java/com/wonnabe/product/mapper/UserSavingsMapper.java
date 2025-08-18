package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * User_Savings 테이블 관련 MyBatis 매퍼 인터페이스
 * 계좌번호 없이 user_id로 직접 연결하는 방식
 */
@Mapper
public interface UserSavingsMapper {
    UserSavingsVO findSavingsDetailByIds(
            @Param("userId") String userId,
            @Param("productId") Long productId
    );

    List<TransactionSummaryDto> findMonthlyTransactionSums(
            @Param("userId") String userId,
            @Param("productId") Long productId,
            @Param("startDate") Date startDate
    );

    List<UserSavingsVO> findAllByUserId(String userId);
}

