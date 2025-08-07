package com.wonnabe.product.service;

import com.wonnabe.product.dto.ProductResponseDTO;

public interface ProductService {
	/**
	 * 시용자 상품 요약 정보 조회
	 * @param userId 사용자 아이디
	 * @return 사용자 상품 요약 정보
	 */
	ProductResponseDTO findUserProductSummary(String userId);
}
