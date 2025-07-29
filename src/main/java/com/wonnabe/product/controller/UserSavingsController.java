package com.wonnabe.product.controller;

import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.service.UserSavingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/savings") // URL 경로를 savings로 명확하게 변경
@RequiredArgsConstructor
@Log4j2
public class UserSavingsController {

    private final UserSavingsService userSavingsService;

    @GetMapping("/{productId}")
    public ResponseEntity<UserSavingsDetailResponseDto> getSavingsDetail(
            @PathVariable("productId") Long productId,
            // @AuthenticationPrincipal CustomUser customuser // 우선 userId를 직접 받도록 수정
            String userId // 임시로 userId를 직접 받도록 수정, 추후 Spring Security 적용
    ) {
        // String userId = customuser.getUser().getUserId(); // 추후 Spring Security 적용

        UserSavingsDetailResponseDto savingsDetail = userSavingsService.getSavingsDetail(userId, productId);

        if (savingsDetail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(savingsDetail);
    }
}