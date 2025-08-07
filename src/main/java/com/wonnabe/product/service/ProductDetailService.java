package com.wonnabe.product.service;

import com.wonnabe.product.dto.InsuranceProductDetailResponseDTO;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;

/*
* 상세보기!!!
* 1) 예적금: SavingsProductDetailResponseDto
* 2) 보험: InsuranceProductDetailResponseDto
 */
public interface ProductDetailService {
    SavingsProductDetailResponseDto getSavingProductDetail(String productId);
    InsuranceProductDetailResponseDTO getInsuranceProductDetail(String productId);
}
