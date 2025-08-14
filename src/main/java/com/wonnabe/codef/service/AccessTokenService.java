package com.wonnabe.codef.service;

import com.wonnabe.codef.client.CodefClient;
import com.wonnabe.codef.dto.auth.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.wonnabe.codef.util.RetryUtils.retryWithBackoff;

@Component
@RequiredArgsConstructor
public class AccessTokenService {

    private final CodefClient codefAuthClient;
    private AccessTokenResponse cachedToken;

    public String issueAccessToken() {
        cachedToken = retryWithBackoff(
                () -> codefAuthClient.requestAccessToken(),
                3, 1000
        );
        return cachedToken.getAccessToken();
    }

    public long getExpiresIn() {
        return Long.parseLong(cachedToken.getExpiresIn());
    }
}
