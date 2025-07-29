// /login - 카카오 로그인 페이지로 & /callback - 카카오 로그인 성공 후 사용자 정보 받아서 JWT 발급
package com.wonnabe.auth.kakao.controller;

import com.wonnabe.auth.dto.TokenResponseDTO;
import com.wonnabe.auth.kakao.dto.KakaoTokenResponse;
import com.wonnabe.auth.kakao.service.KakaoService;
import com.wonnabe.auth.service.AuthService;
import com.wonnabe.user.domain.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth/kakao")
public class KakaoController {

    // KakaoService - 카카오 API 호출 (토큰 발급, 유저 정보)
    // AuthService - 우리 서버 유저 처리 (DB, 토큰 생성)
    private final KakaoService kakaoService;
    private final AuthService authService;

    // 카카오 로그인 창으로 이동하는 URL
    // 사용자가 --/login에 접속하면, kakaoService에서 만든 카카오 로그인 URL로 redirect됨
    @GetMapping("/login")
    public String redirectToKakaoLogin() {
        return "redirect:" + kakaoService.getKakaoLoginUrl();
    }

    // 사용자가 카카오 로그인에 성공하면 카카오가 우리 서버의 /callback 주소로 인가 코드를 전달해줌
    // 그 code를 받아서 처리 시작하는 API
    // response는 나중에 쿠키를 심거나 응답을 구성할 때 사용
    @GetMapping("/callback")
    public ResponseEntity<TokenResponseDTO> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {
        // 카카오 유저 정보 받아오기 by kakaoService
        // code를 이용해 카카오 서버에 accessToken 요청. 그 토큰으로 유저 정보를 받아옴
        var kakaoUser = kakaoService.getKakaoUserInfo(code);

        // DB에 해당 유저가 있는지 확인하거나 없으면 생성 by authService
        // 우리 서비스에 이미 가입된 유저인지 확인하고 없으면 자동 회원가입 진행
        User user = authService.findOrCreateKakaoUser(kakaoUser);

        // JWT 토큰 생성
        // 로그인 완료되면 accessToken + refreshToken 발급
        // 이 토큰 정보는 TokenResponseDTO에 담겨있음
        TokenResponseDTO tokens = authService.login(user);

        // 발급된 토큰들을 클라이언트(프론트)로 JSON 응답으로 내려줌
        // 이걸 프론트가 받아서 → 저장하고 → API 요청할 때 사용하면 됨
        return ResponseEntity.ok(tokens);
    }
}