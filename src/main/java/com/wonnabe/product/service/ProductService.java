package com.wonnabe.product.service;

import com.wonnabe.product.dto.SavingsProductDetailResponseDto;

/*
* 상세보기!!!
 */
public interface ProductService {
    SavingsProductDetailResponseDto getSavingProductDetail(String productId);
}
