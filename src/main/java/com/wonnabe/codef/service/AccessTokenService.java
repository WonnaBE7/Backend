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

    /**
     * CODEF 액세스 토큰을 발급받습니다.
     * 내부적으로 최대 3회까지 지수형 백오프(초기 1000ms)로 재시도하며,
     * 성공하면 응답을 캐시하고 액세스 토큰 문자열을 반환합니다.
     *
     * @return 발급된 액세스 토큰 문자열
     * @implNote 성공 시 {@code cachedToken} 필드에 마지막 응답이 저장됩니다.
     */
    public String issueAccessToken() {
        cachedToken = retryWithBackoff(
                () -> codefAuthClient.requestAccessToken(),
                3, 1000
        );
        return cachedToken.getAccessToken();
    }

    /**
     * 마지막으로 발급받은 액세스 토큰의 유효 기간(초)을 반환합니다.
     *
     * @return 토큰 만료까지 남은 시간(초)
     * @throws NullPointerException       이전에 {@link #issueAccessToken()} 이 호출되지 않아 {@code cachedToken}이 null인 경우
     * @throws NumberFormatException      {@code cachedToken.getExpiresIn()} 값이 정수로 변환 불가능한 경우
     *
     * @apiNote 이 메서드를 호출하기 전에 반드시 {@link #issueAccessToken()} 으로 토큰을 발급해 두세요.
     */
    public long getExpiresIn() {
        return Long.parseLong(cachedToken.getExpiresIn());
    }
}
