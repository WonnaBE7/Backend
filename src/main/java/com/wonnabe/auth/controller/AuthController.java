package com.wonnabe.auth.controller;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.service.AuthService;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 사용자의 회원가입 요청을 처리합니다.
     *
     * @param signupDTO 회원가입 정보 (이름, 이메일, 비밀번호 등)
     * @return 회원가입 성공 시 200 OK, 중복 이메일일 경우 409 CONFLICT
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDTO signupDTO) {
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
}
