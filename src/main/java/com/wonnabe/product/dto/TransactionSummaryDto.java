package com.wonnabe.product.dto;

import lombok.Data;

/**
 * MyBatis 쿼리 결과를 담기 위한 DTO입니다.
 * User_Transactions 테이블에서 특정 사용자의 거래 내역을 월별로 합산한 결과를 매핑합니다.
 */
@Data
public class TransactionSummaryDto {

    /** 거래 월 (형식: "yyyy-MM") */
    private String month;

    /** 해당 월의 총 거래액 합계 */
    private Long totalSavings;
}