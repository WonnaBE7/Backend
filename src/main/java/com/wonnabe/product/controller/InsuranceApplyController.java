package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.InsuranceApplyRequestDTO;
import com.wonnabe.product.service.InsuranceApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/insurances")
@Log4j2
@RequiredArgsConstructor
public class InsuranceApplyController {

    private final InsuranceApplyService insuranceApplyService;

    /**
     * 사용자 보험 신청 API
     * @param insuranceApplyRequestDTO 신청하는 보험에 대한 정보
     * @param customUser 보험을 신청할 사용자 정보
     * @return ResponseEntity<Object>
     */
    @PostMapping("/apply")
    public ResponseEntity<Object> applyInsurance(
            @RequestBody InsuranceApplyRequestDTO insuranceApplyRequestDTO,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        if (insuranceApplyRequestDTO == null || insuranceApplyRequestDTO.getInsuranceId() == null) {
            throw new IllegalArgumentException("잘못된 입력 양식입니다.");
        }

        String userId = customUser.getUser().getUserId();
        insuranceApplyService.applyUserInsurance(insuranceApplyRequestDTO, userId);

        return JsonResponse.ok("보험 신청이 완료되었습니다.");
    }
}
