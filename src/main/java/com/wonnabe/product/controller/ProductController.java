package com.wonnabe.product.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.ProductResponseDTO;
import com.wonnabe.product.service.ProductService;


@RestController
@RequestMapping("/api/user/products")
public class ProductController {

	private final ProductService service;

	public ProductController(@Qualifier("productServiceImpl") ProductService service) {
		this.service = service;
	}

	/**
	 * 사용자 상품 정보 요약 조회
	 * @param user 로그인한 사용자
	 * @return 사용자 상품 정보 요약
	 */
	@GetMapping("/summary")
	public ResponseEntity<Object> getUserProductSummary(@AuthenticationPrincipal CustomUser user) {
		String userId = user.getUser().getUserId();

		ProductResponseDTO products = service.findUserProductSummary(userId);

		return JsonResponse.ok("현재 보유 상품 조회 성공", products);
	}
}
