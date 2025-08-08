package com.wonnabe.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateRequestDTO {

    @NotBlank(message = "목표 이름(goalName)은 필수입니다.")
    private String goalName;

    @NotNull(message = "카테고리 ID(categoryId)는 필수입니다.")
    private Integer categoryId;

    @NotNull(message = "목표 금액(targetAmount)은 필수입니다.")
    @DecimalMin(value = "1", message = "목표 금액(targetAmount)은 1 이상이어야 합니다.")
    private BigDecimal targetAmount;

    @NotNull(message = "목표 기간(goalDurationMonths)은 필수입니다.")
    @Positive(message = "목표 기간(goalDurationMonths)은 1 이상이어야 합니다.")
    private Integer goalDurationMonths;
}
