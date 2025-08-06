package com.wonnabe.nowme.controller;

import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.dto.NowMeResponseDTO;
import com.wonnabe.nowme.service.NowMeService;

// Spring Security에서 로그인한 사용자 정보를 가져오기 위한 도메인 및 어노테이션
import com.wonnabe.common.security.account.domain.CustomUser;
import lombok.RequiredArgsConstructor;

// // 스프링 웹 애노테이션
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.Map;

/**
 * NowMe 진단 API 컨트롤러
 * - 사용자의 금융 성향을 분석하는 NowMe 진단 결과를 응답하는 API 엔드포인트
 */

@RestController // REST API 컨트롤러임을 명시 (JSON 응답)
@RequestMapping("/api/nowme") // 공통 URL Prefix 설정
@RequiredArgsConstructor // final 필드 자동 생성자 주입 (nowMeService)
@Log4j2
public class NowMeController {

    private final NowMeService nowMeService;

    @PostMapping("/diagnosis")
    public ResponseEntity<?> diagnose(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody NowMeRequestDTO request
    ) {
        try {
            log.info("NowMe 진단 API 요청 - userId: {}", user.getUser().getUserId());

            String userId = user.getUser().getUserId();
            NowMeResponseDTO result = nowMeService.diagnose(userId, request);

            if (result.isSuccess()) {
                return JsonResponse.ok("진단이 완료되었습니다.", result);
            } else {
                return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "진단 처리 중 오류가 발생했습니다.");
            }

        } catch (Exception e) {
            log.error("NowMe 진단 처리 실패", e);
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "진단 처리 중 오류가 발생했습니다.");
        }
    }
}