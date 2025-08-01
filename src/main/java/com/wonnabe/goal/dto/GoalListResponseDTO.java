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
public class GoalListResponseDTO {
    private Integer totalGoalCount;
    private BigDecimal totalTargetAmount;
    private List<GoalSummaryResponseDTO> goals;
}
