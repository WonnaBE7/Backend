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

    /**
     * 로그인 성공 시 호출되는 메서드입니다.
     * AccessToken/RefreshToken을 발급하고, RefreshToken을 Redis에 저장한 뒤 HttpOnly·Secure 쿠키로 설정합니다.
     * 또한 자산 동기화를 소프트 타임아웃 내에서 시도하며, 지연/실패와 무관하게 로그인 흐름은 계속 진행됩니다.
     *
     * @param request  클라이언트 요청 객체
     * @param response 서버 응답 객체
     * @param authentication 인증 성공 시의 사용자 인증 정보
     * @throws IOException JSON 직렬화 또는 응답 작성 중 예외 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUser user = (CustomUser) authentication.getPrincipal();
        String userId = user.getUser().getUserId();

        try {
            assetSyncOrchestrator.syncWithSoftTimeout(userId, SOFT_SYNC_TIMEOUT);
        } catch (Exception e) {
            log.warn("asset sync short attempt failed, proceed login - userId={}, err={}", userId, e.toString());
        }

        String refreshToken = jwtProcessor.generateRefreshToken(userId);
        boolean rtSaved = false;

        try {
            refreshTokenRedisRepository.save(userId, refreshToken, 60 * 60 * 24 * 7);
            rtSaved = true;
        } catch (Exception e) {
            log.error("Redis 저장 실패 - userId: {}, error: {}", userId, e.getMessage());
        }

        if (!rtSaved) {
            // 엄격 모드: RT 보관 실패 == 로그인 실패
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "code", 503,
                    "message", "로그인 실패: 세션 저장 오류(잠시 후 다시 시도해주세요)"
            ));
            return;
        }

        String accessToken = jwtProcessor.generateAccessToken(userId);

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
                "data", new AuthResultDTO(accessToken, UserInfoDTO.of(user.getUser()))
        ));
        log.info("✅ 로그인 성공 - userId={}", userId);
    }
}
