package com.wonnabe.codef.service;

import com.wonnabe.codef.util.CodefManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Log4j2
public class CodefAuthService {

    private final CodefManager accessTokenManager;

    /**
     * 사용자별 CODEF 액세스 토큰을 확인하고, 만료된 경우 갱신합니다.
     * 갱신 과정에서 발생한 예외는 전파하지 않고 경고 로그만 남깁니다
     * (상위 흐름은 중단하지 않기 위함).
     *
     * @param userId CODEF 토큰을 점검/갱신할 사용자 식별자
     */
    public void syncUserCodef(String userId) {
        try {
            accessTokenManager.refreshCodefIfExpired(userId);
        } catch (Exception e) {
            log.warn("⚠️ CODEF 토큰 갱신 중 오류 발생 - userId: {}, message: {}", userId, e.getMessage());
        }
    }
}


