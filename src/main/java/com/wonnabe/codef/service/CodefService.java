package com.wonnabe.codef.service;

import com.wonnabe.codef.util.CodefManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Log4j2
public class CodefService {

    private final CodefManager accessTokenManager;

    public void refreshAllExpiredTokens(String userId) {
        try {
            accessTokenManager.validateAndRefreshTokens(userId);
        } catch (Exception e) {
            log.warn("⚠️ CODEF 토큰 갱신 중 오류 발생 - userId: {}, message: {}", userId, e.getMessage());
        }
    }
}


