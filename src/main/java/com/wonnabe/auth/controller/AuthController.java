package com.wonnabe.auth.controller;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.service.AuthService;
import com.wonnabe.auth.service.KakaoService;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private KakaoService kakaoService;

    /**
     * 사용자의 회원가입 요청을 처리합니다.
     *
     * @param signupDTO 회원가입 정보 (이름, 이메일, 비밀번호 등)
     * @return 회원가입 성공 시 200 OK, 중복 이메일일 경우 409 CONFLICT
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDTO signupDTO) {
        // 입력값 검증 추가 가능
        if (signupDTO.getEmail() == null || signupDTO.getPassword() == null) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, "필수 정보가 누락되었습니다.");
        }

        boolean result = authService.registerUser(signupDTO);
        return result
                ? JsonResponse.ok("회원가입 성공")
                : JsonResponse.error(HttpStatus.CONFLICT, "이미 가입된 이메일입니다");
    }

    /**
     * 클라이언트가 쿠키에 담아 보낸 Refresh Token을 기반으로 새로운 Access Token을 발급합니다.
     *
     * @param request  HTTP 요청 객체 (쿠키 포함)
     * @param response HTTP 응답 객체
     * @return 토큰이 유효하면 새로운 Access Token과 사용자 정보 반환, 그렇지 않으면 401 UNAUTHORIZED
     */
    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthResultDTO result = authService.refreshAccessToken(request, response);
            return JsonResponse.ok("Access Token 재발급 완료", result);
        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * 사용자의 로그아웃 요청을 처리합니다.
     * - Redis에서 refresh token 제거
     * - 브라우저의 쿠키 삭제
     *
     * @param request  HTTP 요청 객체 (쿠키 포함)
     * @param response HTTP 응답 객체
     * @return 로그아웃 성공 시 200 OK 반환, 실패 시 400 BAD REQUEST
     */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.logout(request, response);
            return JsonResponse.ok("로그아웃이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 카카오 로그인 URL 반환
     */
    @GetMapping("/kakao/login-url")
    public ResponseEntity<?> getKakaoLoginUrl() {
        try {
            String loginUrl = kakaoService.getKakaoLoginUrl();
            Map<String, String> result = new HashMap<>();
            result.put("loginUrl", loginUrl);
            return JsonResponse.ok("카카오 로그인 URL 생성 성공", result);
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인 URL 생성 실패");
        }
    }

    /**
     * 카카오 로그인 콜백 처리
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code,
                                           @RequestParam(value = "error", required = false) String error,
                                           HttpServletResponse response) {

        if (error != null) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, "카카오 로그인이 취소되었습니다.");
        }

        if (code == null || code.isEmpty()) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, "인증 코드가 없습니다.");
        }

        try {
            // 기존 AuthService의 카카오 로그인 메서드 호출
            AuthResultDTO result = authService.processKakaoLogin(code, response);
            return JsonResponse.ok("카카오 로그인 성공", result);

        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * SPA에서 직접 코드를 보내는 경우
     */
    @PostMapping("/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> request,
                                        HttpServletResponse response) {

        String code = request.get("code");

        if (code == null || code.isEmpty()) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, "인증 코드가 필요합니다.");
        }

        try {
            AuthResultDTO result = authService.processKakaoLogin(code, response);
            return JsonResponse.ok("카카오 로그인 성공", result);

        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }
}
