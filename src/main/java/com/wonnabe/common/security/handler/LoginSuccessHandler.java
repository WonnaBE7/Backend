package com.wonnabe.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.codef.service.AssetSyncOrchestrator;
import com.wonnabe.auth.repository.RefreshTokenRedisRepository;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.security.account.dto.UserInfoDTO;
import com.wonnabe.common.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProcessor jwtProcessor;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final AssetSyncOrchestrator assetSyncOrchestrator;

    private static final Duration SOFT_SYNC_TIMEOUT = Duration.ofSeconds(5);
    private final long softTimeoutMs = SOFT_SYNC_TIMEOUT.toMillis();

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

        boolean syncedNow = false;
        try {
            syncedNow = assetSyncOrchestrator.syncWithSoftTimeout(userId, Duration.ofMillis(softTimeoutMs));
        } catch (Exception e) {
            log.warn("asset sync short attempt failed, proceed login - userId={}, err={}", userId, e.toString());
        }

        String accessToken = jwtProcessor.generateAccessToken(userId);
        String refreshToken = jwtProcessor.generateRefreshToken(userId);
        try {
            refreshTokenRedisRepository.save(userId, refreshToken, 60 * 60 * 24 * 7);
        } catch (Exception e) {
            log.error("Redis 저장 실패 - userId: {}, error: {}", userId, e.getMessage());
        }

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(refreshCookie);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), Map.of(
                "code", 200,
                "message", "로그인 성공",
                "data", new AuthResultDTO(accessToken, UserInfoDTO.of(user.getUser())),
                "assetSyncInProgress", !syncedNow
        ));
        log.info("✅ 로그인 성공 - userId={}, assetSyncInProgress={}", userId, !syncedNow);
    }
}
