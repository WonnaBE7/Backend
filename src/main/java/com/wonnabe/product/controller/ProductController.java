package com.wonnabe.product.controller;

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
    public ResponseEntity<SavingsProductDetailResponseDto> getSavingProductDetail(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getSavingProductDetail(productId));
    }
}
