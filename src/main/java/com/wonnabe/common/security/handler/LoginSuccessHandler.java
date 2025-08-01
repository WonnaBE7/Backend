package com.wonnabe.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.codef.service.CodefService;
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
    private final CodefService codefAuthService;

    /**
     * 사용자 정보와 AccessToken을 포함하는 AuthResultDTO 객체를 생성합니다.
     *
     * @param user        CustomUser 객체
     * @param accessToken 발급된 AccessToken
     * @return AuthResultDTO 객체
     */
    private AuthResultDTO makeAuthResult(CustomUser user, String accessToken) {
        return new AuthResultDTO(accessToken, UserInfoDTO.of(user.getUser()));
    }

    /**
     * 로그인 성공 시 실행되는 메서드입니다.
     * - AccessToken, RefreshToken 발급
     * - RefreshToken Redis 저장 및 쿠키로 전송
     * - 사용자 정보와 함께 JSON 응답 반환
     *
     * @param request        HTTP 요청 객체
     * @param response       HTTP 응답 객체
     * @param authentication 인증 정보 객체
     * @throws IOException JSON 응답 처리 중 오류 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUser user = (CustomUser) authentication.getPrincipal();
        String userId = user.getUser().getUserId();

        // ✅ CODEF access token 갱신 (예외 발생 시 무시하고 계속 진행)
        try {
            codefAuthService.refreshAllExpiredTokens(userId);
        } catch (Exception e) {
            log.warn("⚠️ CODEF 갱신 실패 - userId: {}, error: {}", userId, e.getMessage());
        }

        // ✅ 토큰 생성
        String accessToken = jwtProcessor.generateAccessToken(userId);
        String refreshToken = jwtProcessor.generateRefreshToken(userId);

        // ✅ Redis 저장
        try {
            refreshTokenRedisRepository.save(userId, refreshToken, 60 * 60 * 24 * 7);
        } catch (Exception e) {
            log.error("❌ Redis 저장 실패 - userId: {}, error: {}", userId, e.getMessage());
        }

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
