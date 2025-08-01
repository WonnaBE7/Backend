package com.wonnabe.codef.service;

import com.wonnabe.codef.client.CodefClient;
import com.wonnabe.codef.domain.CodefAuthEntity;
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

    public String issueConnectedId(CodefAuthEntity auth, String accessToken) {
        Map<String, Object> accountMap = requestBuilder.buildAccountCreateRequest(auth);

        return retryWithBackoff(() ->
                        codefAuthClient.requestConnectedId(accessToken, accountMap).getData().getConnectedId(),
                3, 1000
        );
    }
}
