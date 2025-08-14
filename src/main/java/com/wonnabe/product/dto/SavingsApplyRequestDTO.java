package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsApplyRequestDTO {
    private Long productId;         // 상품 ID
    private Long principalAmount;   // 목표금액 !!!!!
    private Long monthlyPayment;    // 월 납입액 (적금)
    private Integer joinPeriod;     // 가입 기간 (개월)
}
