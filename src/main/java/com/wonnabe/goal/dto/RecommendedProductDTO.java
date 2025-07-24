package com.wonnabe.goal.dto;

import java.math.BigDecimal;
import java.util.List;

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
