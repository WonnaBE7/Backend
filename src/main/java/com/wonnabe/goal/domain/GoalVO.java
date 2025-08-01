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
public class GoalVO {
    private Long id;
    private String userId;
    private Integer nowmeId;
    private Integer categoryId;
    private String goalName;
    private BigDecimal targetAmount;
    private Integer goalDurationMonths;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Long selectedProductsId;
    private BigDecimal currentAmount;
    private BigDecimal saveAmount;
    private BigDecimal expectedTotalAmount;
    private BigDecimal progressRate;
    private Boolean isAchieved;
    private LocalDateTime achievedDate;
    private String resultSummary;
    private String status; // DRAFT, PUBLISHED, ACHIEVED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
