package com.wonnabe.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateRequestDTO {
    private String goalName;
    private Integer categoryId;
    private BigDecimal targetAmount;
    private Integer goalDurationMonths;
}
