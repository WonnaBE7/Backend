package com.wonnabe.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateResponseDTO {
    private Long goalId;
    private String futureMeMessage;
    private List<RecommendedProductDTO> recommendedProducts;
}
