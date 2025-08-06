package com.wonnabe.codef.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserSaving {
    private Long id;
    private String userId;
    private Long productId;
    private BigDecimal principalAmount;
    private Date startDate;
    private Date maturityDate;
    private BigDecimal monthlyPayment;
}