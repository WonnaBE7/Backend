package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.service.UserSavingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/savings")
@RequiredArgsConstructor
@Log4j2
public class UserSavingsController {

    private final UserSavingsService userSavingsService;

    @GetMapping("/{productId}")
    public ResponseEntity<UserSavingsDetailResponseDto> getSavingsDetail(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal CustomUser customUser
    ) {

        String userId = customUser.getUser().getUserId();
        UserSavingsDetailResponseDto savingsDetail = userSavingsService.getSavingsDetail(userId, productId);

        if (savingsDetail == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(savingsDetail);
    }
}
