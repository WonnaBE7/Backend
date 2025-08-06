package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.SavingsApplyRequestDTO;
import com.wonnabe.product.service.SavingsApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/savings")
@Log4j2
@RequiredArgsConstructor
public class SavingsApplyController {

    private final SavingsApplyService savingsApplyService;

    /**
     * 사용자 예적금 신청 API
     * @param requestDTO 신청하는 예적금에 대한 정보
     * @param customUser 예적금을 신청할 사용자 정보
     * @return ResponseEntity<Object>
     */
    @PostMapping("/apply")
    public ResponseEntity<Object> applySavings(
            @RequestBody SavingsApplyRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        if (requestDTO == null || requestDTO.getProductId() == null) {
            throw new IllegalArgumentException("잘못된 입력 양식입니다.");
        }

        String userId = customUser.getUser().getUserId();
        savingsApplyService.applyUserSavings(requestDTO, userId);

        return JsonResponse.ok("예적금 신청이 완료되었습니다.");
    }
}
