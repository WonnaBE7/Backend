package com.wonnabe.asset.mapper;

import com.wonnabe.asset.dto.TransactionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsumptionTransactionsMapper {

    /**
     * 특정 월의 거래내역 조회
     */
    List<TransactionDTO> getMonthlyTransactions(
            @Param("userId") String userId,
            @Param("yearMonth") String yearMonth
    );

    // 카테고리별 상세 거래 내역
    List<TransactionDTO> getTransactionsByCategory(
            @Param("userId") String userId,
            @Param("category") String category
    );
}
