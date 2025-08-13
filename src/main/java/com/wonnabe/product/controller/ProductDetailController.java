package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.InsuranceProductDetailResponseDTO;
import com.wonnabe.product.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @GetMapping("/savings/{productId}")
    public ResponseEntity<Object> getSavingProductDetail(@PathVariable String productId) {
        return JsonResponse.ok("예적금 상품 상세 조회 성공", productDetailService.getSavingProductDetail(productId));
    }

    @GetMapping("/insurances/{productId}")
    public ResponseEntity<Object> getInsuranceProductDetail(
            @PathVariable String productId,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        InsuranceProductDetailResponseDTO insuranceProductDetail = productDetailService.getInsuranceProductDetail(productId);
        return JsonResponse.ok("보험상품 상세 조회 성공", insuranceProductDetail);
    }
}
