package com.wonnabe.codef.service;

import com.wonnabe.codef.client.CodefClient;
import com.wonnabe.codef.domain.CodefAuth;
import com.wonnabe.codef.util.AccountRequestBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.wonnabe.codef.util.RetryUtils.retryWithBackoff;

@Component
@RequiredArgsConstructor
public class ConnectedIdService {

    private final CodefClient codefAuthClient;
    private final AccountRequestBuilder requestBuilder;

    /**
     * CODEF에 계정 등록을 요청하여 Connected ID를 발급합니다.
     * 내부적으로 {@link AccountRequestBuilder} 를 사용해 요청 바디를 생성하고,
     * {@link CodefClient#requestConnectedId(String, Map)} 호출을
     * 지수형 백오프(초기 1000ms)로 최대 3회까지 재시도합니다.
     *
     * @param auth         계정 등록에 필요한 사용자/기관 인증 정보
     * @param accessToken  CODEF 액세스 토큰(Bearer)
     * @return 발급된 Connected ID 문자열
     *
     * @throws RuntimeException  재시도 횟수 내에 성공하지 못한 경우(하위 클라이언트 예외 전파)
     *
     * @implNote 재시도 정책은 {@link com.wonnabe.codef.util.RetryUtils#retryWithBackoff} 에 의해 적용됩니다.
     *           성공 시 응답의 data.connectedId 값을 반환합니다.
     */
    public String issueConnectedId(CodefAuth auth, String accessToken) {
        Map<String, Object> accountMap = requestBuilder.buildAccountCreateRequest(auth);

        return retryWithBackoff(() ->
                        codefAuthClient.requestConnectedId(accessToken, accountMap).getData().getConnectedId(),
                3, 1000
        );
    }
}
