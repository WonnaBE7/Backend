package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceApplyRequestDTO {
    private String productType; // 상품의 종류 (insurance)
    private String insuranceId; // 보험 상품의 아이디
}