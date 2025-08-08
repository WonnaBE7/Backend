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
public class GoalDetailResponseDTO {
    private Long id;
    private String goalName;
    private String categoryName;
    private String nowmeName;
    private BigDecimal progressRate;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Integer goalDurationMonths;
    private Integer remainingMonths;
    private BigDecimal saveAmount;
    private String futureMeMessage;
    private Long selectedProductId;
    private List<RecommendedProductDTO> recommendedProducts;
}
