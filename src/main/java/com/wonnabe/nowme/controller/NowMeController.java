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

import java.util.HashMap;
import java.util.Map;

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
//    public NowMeResponseDTO diagnose(
//            @AuthenticationPrincipal CustomUser user,
//            @RequestBody NowMeRequestDTO request
//    ) {
//        // 실제 운영시 사용할 코드 (String userId 직접 사용)
//        String userId = user.getUser().getUserId();
//        return nowMeService.diagnose(userId, request);
//    }
    public NowMeResponseDTO diagnose(@RequestBody NowMeRequestDTO request) {
        // 실제 DB 사용자 ID (String)
        String testUserId = "f6789012-3456-7890-abcd-mn12op34qr56";
//        String testUserId = "a1b2c3d4-e5f6-7890-ab12-cd34ef56gh78";
//        String testUserId = "b2c3d4e5-f678-9012-abcd-ef12gh34ij56";
        System.out.println("⭐⭐⭐⭐⭐ NowMe API 호출됨!");
//        String testUserId = "h8901234-5678-9012-abcd-qr56st78uv90";
//        String testUserId = "111";
        return nowMeService.diagnose(testUserId, request);
    }

    /**
     * 🔸 모든 사용자 한번에 테스트
     */
    @PostMapping("/test/all")
    public Map<String, NowMeResponseDTO> testAllUsers(@RequestBody NowMeRequestDTO request) {
        Map<String, NowMeResponseDTO> results = new HashMap<>();

        String[] userIds = {"111", "222", "333", "444", "555"};
        String[] userNames = {"자린고비형", "YOLO형", "공격투자형", "소확행형", "느긋한관망형"};

        for (int i = 0; i < userIds.length; i++) {
            try {
                NowMeResponseDTO result = nowMeService.diagnose(userIds[i], request);
                results.put(userNames[i] + "(" + userIds[i] + ")", result);
                System.out.println("✅ " + userNames[i] + ": " + result.getPersonaName());
            } catch (Exception e) {
                System.out.println("❌ " + userNames[i] + " 테스트 실패: " + e.getMessage());
            }
        }
        return results;
    }

//    public NowMeResponseDTO diagnose(@RequestBody NowMeRequestDTO request) {
//        System.out.println("🔥🔥🔥 API 호출됨!"); // 강력한 로그
//        String testUserId = "h8901234-5678-9012-abcd-qr56st78uv90";
//
//        try {
//            System.out.println("🔥🔥🔥 서비스 호출 전");
//            NowMeResponseDTO result = nowMeService.diagnose(testUserId, request);
//            System.out.println("🔥🔥🔥 서비스 호출 후: " + result.getPersonaName());
//            return result;
//        } catch (Exception e) {
//            System.out.println("🔥🔥🔥 에러 발생: " + e.getMessage());
//            e.printStackTrace();
//            return NowMeResponseDTO.failure();
//        }
//    }

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