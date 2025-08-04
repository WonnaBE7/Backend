package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.UserInsuranceDetailDTO;
import com.wonnabe.product.service.CardService;
import com.wonnabe.product.service.UserInsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Log4j2
public class UserInsuranceController {

    private final UserInsuranceService userInsuranceService;

    public UserInsuranceController(@Qualifier("UserInsuranceServiceImpl") UserInsuranceService userInsuranceService) {
        this.userInsuranceService = userInsuranceService;
    }

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

        // 아이디를 가져옴
        String userId = customUser.getUser().getUserId();

        // Service에서 만든 함수로 DTO를 가져옴
        UserInsuranceDetailDTO responseDTO = userInsuranceService.getDetailByProductId(userId, productId);

        if (responseDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(responseDTO);
    }
}
