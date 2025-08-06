package com.wonnabe.codef.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserInsurance {

    private Long id;
    private String userId;
    private Long productId;
    private BigDecimal monthlyPremium;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPaid;
    private LocalDateTime createdAt;
}
