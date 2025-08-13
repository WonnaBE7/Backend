package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 월별 보험 수령액(입금) 데이터를 담는 DTO입니다. 08.12ver
 * Mapper에서 조회된 월별 데이터를 서비스 계층으로 전달하는 데 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyInsuranceReceiptDto {

    /** 거래 월 (형식: "yyyy-MM") */
    private String month;

    /** 해당 월의 총 수령액 (입금) */
    private Long amount;
}
