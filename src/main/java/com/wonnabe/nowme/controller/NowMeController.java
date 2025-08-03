package com.wonnabe.nowme.controller;

// 진단api 요청/응답 DTO와 서비스 클래스 import
import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.dto.NowMeResponseDTO;
import com.wonnabe.nowme.service.NowMeService;

// Spring Security에서 로그인한 사용자 정보를 가져오기 위한 도메인 및 어노테이션
import com.wonnabe.common.security.account.domain.CustomUser;
import lombok.RequiredArgsConstructor;

// // 스프링 웹 애노테이션
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * NowMe 진단 API 컨트롤러
 * - 사용자의 금융 성향을 분석하는 NowMe 진단 결과를 응답하는 API 엔드포인트
 */

@RestController // REST API 컨트롤러임을 명시 (JSON 응답)
@RequestMapping("/api/nowme") // 공통 URL Prefix 설정
@RequiredArgsConstructor // final 필드 자동 생성자 주입 (nowMeService)
public class NowMeController {

    // 진단 로직을 수행하는 서비스 클래스 주입
    private final NowMeService nowMeService;

    /**
     * NowMe 페르소나 진단 요청 API
     * - 로그인한 사용자의 ID와 설문 응답 데이터를 받아 진단 결과를 반환함
     *
     * @param user 로그인한 사용자 정보 (Spring Security가 자동 주입)
     * @param request 클라이언트가 보낸 진단 설문 응답 DTO
     * @return 진단 결과 DTO (이름, 유형 등)
     */
    @PostMapping("/diagnosis")
    public NowMeResponseDTO diagnose(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody NowMeRequestDTO request
    ) {
        // user.getUser().getUserId()로 접근 후 Long으로 변환
        String userIdStr = user.getUser().getUserId();
        Long userId = Long.parseLong(userIdStr);

        return nowMeService.diagnose(userId, request);
    }

    /*
    프로토타입용 (Security 적용 X)
    @PostMapping("/diagnosis")
    public NowMeResponseDTO diagnose(@RequestBody NowMeRequestDTO request) {
    return nowMeService.diagnose(request.getUserId(), request);
}

    * */
}

/*
[클라이언트 요청]
    |
    | POST /api/nowme/diagnosis
    | Body: NowMeRequestDTO (설문 응답)
    |
[NowMeController]
    |
    └─> nowMeService.diagnose(userId, request)
                |
                └─> 진단 로직 수행 → 페르소나 분석 결과 반환
* */