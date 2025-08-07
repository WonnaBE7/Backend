package com.wonnabe.product.controller;

import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;
import com.wonnabe.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/savings/{productId}")
    public ResponseEntity<Object> getSavingProductDetail(@PathVariable String productId) {
        return JsonResponse.ok("예적금 상품 상세 조회 성공", productService.getSavingProductDetail(productId));
    }
}
