package com.wonnabe.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.auth.repository.RefreshTokenRedisRepository;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.security.account.dto.UserInfoDTO;
import com.wonnabe.common.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProcessor jwtProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    private AuthResultDTO makeAuthResult(CustomUser user, String accessToken) {
        return new AuthResultDTO(accessToken, UserInfoDTO.of(user.getUser()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUser user = (CustomUser) authentication.getPrincipal();
        String userId = user.getUser().getUserId();

        // ✅ 토큰 생성
        String accessToken = jwtProcessor.generateAccessToken(userId);
        String refreshToken = jwtProcessor.generateRefreshToken(userId);

        // ✅ Redis 저장
        refreshTokenRedisRepository.save(userId, refreshToken, 60 * 60 * 24 * 7); // 7일 (초 단위)

        // ✅ Refresh Token → Secure HttpOnly 쿠키로 전송
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(refreshCookie);

        // ✅ 응답 데이터 구성
        AuthResultDTO result = makeAuthResult(user, accessToken);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("message", "로그인 성공");
        body.put("data", result);

        // ✅ JSON 직렬화 및 응답 전송
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), body);

        log.info("✅ 로그인 성공 - userId: {}", userId);
    }
}
