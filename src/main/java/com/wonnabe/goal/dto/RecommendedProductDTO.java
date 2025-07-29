package com.wonnabe.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProductDTO {
    private Long id;
    private String name;
    private String bank;
    private List<String> tags;
    private Float interestRate;
    private Integer achievementRate;
    private BigDecimal monthlyDepositAmount;
    private String expectedAchievementDate;
    private BigDecimal expectedTotalAmount;
}
