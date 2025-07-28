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
}
