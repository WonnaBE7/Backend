package com.wonnabe.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalSummaryResponseDTO {
    private Long id;
    private String goalName;
    private String categoryName;
    private String nowmeName;
    private Float progressRate;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Integer goalDurationMonths;
    private LocalDate startDate;
    private String status;
}
