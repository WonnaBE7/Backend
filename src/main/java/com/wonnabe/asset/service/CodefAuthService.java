package com.wonnabe.asset.service;

import com.wonnabe.asset.util.AccessTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CodefAuthService {

    private final AccessTokenManager accessTokenManager;

    public void refreshAllExpiredTokens(String userId) {
        accessTokenManager.validateAndRefreshTokens(userId);
    }
}


