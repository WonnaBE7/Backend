package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 월별 자산 추이 차트의 한 단위를 나타내는 DTO입니다. -> 예적금
 * 프론트엔드에서 월별 성장 그래프를 그리는 데 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyChartDto {

    /** 차트에 표시될 월 (예: "1월", "2월", ...) */
    private String month;

    /** 해당 월까지의 누적 자산 (원금 + 이자) */
    private Long amount;

}