package com.wonnabe.goal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wonnabe.goal.domain.GoalVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalSummaryResponseDTO {
    private Long id;
    private String goalName;
    private String categoryName;
    private String nowmeName;
    private Integer progressRate;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Integer goalDurationMonths;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;
    private String status;

    public static GoalSummaryResponseDTO of(GoalVO vo) {
        return vo == null ? null : GoalSummaryResponseDTO.builder()
                .id(vo.getId())
                .goalName(vo.getGoalName())
                .categoryName("") // TODO: common_category DB랑 계산 필요
                .nowmeName("") // TODO: financial_tendency_type DB랑 계산 필요
                .progressRate(vo.getProgressRate() != null ? Math.round(vo.getProgressRate()) : null)
                .targetAmount(vo.getTargetAmount())
                .currentAmount(vo.getCurrentAmount())
                .goalDurationMonths(vo.getGoalDurationMonths())
                .startDate(vo.getStartDate())
                .status(vo.getStatus())
                .build();
    }
}
