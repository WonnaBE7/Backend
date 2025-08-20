package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyConsumptionDTO {
    private String month; // 월
    private Integer amount; // 양
}
