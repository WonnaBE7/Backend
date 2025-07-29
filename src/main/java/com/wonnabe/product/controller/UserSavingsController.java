package com.wonnabe.product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import com.wonnabe.product.dto.ApiResponseDto;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.service.UserSavingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 예적금 상품 REST API 컨트롤러
 * JWT 토큰 추출, 검증, 예외처리, HTTP 상태코드 관리, Log4j2 로깅기능
 *
 * API: /api/user/products/savings/{productId}
 */

@RestController
@RequestMapping("/api/user/products/savings")
@RequiredArgsConstructor
@Log4j2
public class UserSavingsController {

    private final UserSavingsService userSavingsService;

    /**
     * 예적금 상품 상세 조회 API
     * GET /api/user/products/savings/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<UserSavingsDetailResponseDto>> getUserSavingsDetail(
            @PathVariable("productId") Long productId,
            @RequestParam(value= "testUserId", required = false) String testUserId,
            HttpServletRequest request){

        log.info("=== 예적금 상품 상세 조회 API 시작 ===");
        log.info("예적금 상품 조회 - productId: {}, userId: {}", productId,testUserId);

        try {
            // JWT 토큰 추출 및 검증 (임시로 testUserId 사용)
            String userId = extractUserId(testUserId, request);

            log.info("사용자 인증 완료 - userId: {}", userId);

            // 서비스 호출
            UserSavingsDetailResponseDto responseDto =
                    userSavingsService.getSavingsDetail(userId, productId);
            log.info("=== API 응답 성공 ===");

            return ResponseEntity.ok(
                    ApiResponseDto.success("예적금 상품 상세 조회 성공", responseDto)
            );
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 파라미터 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponseDto.error(400, e.getMessage())
            );

        } catch (RuntimeException e) {
            log.error("비즈니스 로직 오류 - {}", e.getMessage());
            return ResponseEntity.status(404).body(
                    ApiResponseDto.error(404, e.getMessage())
            );

        } catch (Exception e) {
            log.error("서버 내부 오류", e);
            return ResponseEntity.status(500).body(
                    ApiResponseDto.error(500, "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * JWT 토큰 추출 및 검증 (임시로 testUserId 사용)
     */
    private String extractUserId(String testUserId, HttpServletRequest request) {
        // TODO: JWT 토큰에서 userId 추출 로직 구현 예정
        // String token = request.getHeader("Authorization");
        // return jwtTokenProvider.getUserId(token);

        // 임시로 testUserId 사용
        if (testUserId == null || testUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT 구현 전까지 testUserId 파라미터가 필요합니다.");
        }

        // UUID 형식 검증 추가
        try {
            UUID.fromString(testUserId.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바른 UUID 형식이 아닙니다: " + testUserId);
        }


        return testUserId.trim();
    }

    /**
     * 테스트용 가이드 API
     */
    @GetMapping("/test/guide")
    public ResponseEntity<ApiResponseDto<Object>> getTestGuide() {
        return ResponseEntity.ok(
                ApiResponseDto.success("테스트 가이드",
                        java.util.Map.of(
                                "testUserId", "test-user-001",
                                "usage", "GET /api/user/products/savings/{productId}?testUserId={사용자ID}",
                                "example", "GET /api/user/products/savings/2001?testUserId=test-user-001"
                        ))
        );
    }
}