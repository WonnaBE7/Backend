package com.wonnabe.product.controller;

import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.service.UserSavingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/savings") // URL 경로를 savings로 명확하게 변경
@RequiredArgsConstructor
@Log4j2
public class UserSavingsController {

    private final UserSavingsService userSavingsService;

    @GetMapping("/{productId}")
    public ResponseEntity<UserSavingsDetailResponseDto> getSavingsDetail(
            @PathVariable("productId") Long productId,
            @RequestParam("userId") String userId // @RequestParam 추가
    ) {
        // String userId = customuser.getUser().getUserId(); // 추후 Spring Security 적용

        UserSavingsDetailResponseDto savingsDetail = userSavingsService.getSavingsDetail(userId, productId);

        if (savingsDetail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(savingsDetail);
    }
}