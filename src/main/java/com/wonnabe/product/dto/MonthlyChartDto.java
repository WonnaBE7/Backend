package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 월별 차트 데이터를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyChartDto {

    private String month;       // 월 (예: "3월")
    private Integer percentage; // 달성률 (예: 85)

}