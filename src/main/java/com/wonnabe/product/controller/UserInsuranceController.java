package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.UserInsuranceDetailDTO;
import com.wonnabe.product.service.UserInsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자의 보유 보험 상품 관련 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/user/insurances")
@RequiredArgsConstructor
public class UserInsuranceController {

    private final UserInsuranceService userInsuranceService;

    /**
     * 현재 로그인한 사용자의 특정 보유 보험 상품 상세 정보를 조회하는 API.
     *
     * @param productId 조회할 보험 상품의 고유 ID
     * @param customUser Spring Security를 통해 주입되는 현재 사용자 정보
     * @return {@link UserInsuranceDetailDTO} 형태의 상세 정보. 데이터가 없으면 404 Not Found.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<UserInsuranceDetailDTO> getMyInsuranceDetail(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUser customUser) {

        String userId = customUser.getUser().getUserId();
        UserInsuranceDetailDTO responseDTO = userInsuranceService.getDetailByProductId(userId, productId);

        if (responseDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(responseDTO);
    }
}
