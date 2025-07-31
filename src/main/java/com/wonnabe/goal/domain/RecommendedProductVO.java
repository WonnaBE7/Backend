package com.wonnabe.goal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProductVO {
    private Long id;
    private Long goalId;
    private Long productId;
    private String productName;
    private String bankName;
    private BigDecimal interestRate;
    private BigDecimal achievementRate;
    private BigDecimal monthlyDepositAmount;
    private LocalDate expectedAchievementDate;
    private BigDecimal expectedTotalAmount;
    private LocalDateTime createdAt;
}
