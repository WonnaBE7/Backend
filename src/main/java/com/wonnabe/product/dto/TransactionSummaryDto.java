package com.wonnabe.product.dto;

import lombok.Data;

/**
 * 월별 차트 계산을 위해 User_Transactions 테이블에서
 * 필요한 최소한의 데이터만 담는 DTO
 */
@Data
public class TransactionSummaryDto {
    private String month;
    private Long totalSavings;
}
