package com.wonnabe.codef.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.codef.dto.AccountListResponse;
import com.wonnabe.codef.dto.CardListWrapper;
import com.wonnabe.codef.dto.CodefAuthParam;
import com.wonnabe.codef.dto.CodefTransactionResponse;
import com.wonnabe.codef.util.AssetRequestBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class CodefApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AssetRequestBuilder assetRequestBuilder;

    public Object fetchRawAccountResponse(CodefAuthParam param) {
        try {
            String url = param.getEndpoint();
            Map<String, Object> body = assetRequestBuilder.buildAssetCreateRequest(param);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(param.getAccessToken());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            String raw = URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8.name());

            return parseApiResponse(url, raw);
        } catch (Exception e) {
            log.error("CODEF API 호출 실패 - 기관: {}, 오류: {}", param.getInstitutionCode(), e.getMessage());
            throw new RuntimeException("CODEF 응답 파싱 실패", e);
        }
    }

    private Object parseApiResponse(String endpoint, String rawJson) throws Exception {
        if (endpoint.contains("/card-list")) {
            // 카드 보유카드 조회
            return objectMapper.readValue(rawJson, CardListWrapper.class);
        } else if (endpoint.contains("/account-list")) {
            // 은행 보유계좌 조회
            return objectMapper.readValue(rawJson, AccountListResponse.class);
        } else if (endpoint.contains("/account/transaction-list")) {
            // 은행 수시입출 거래내역 조회
            return objectMapper.readValue(rawJson, CodefTransactionResponse.class);
        } else {
            throw new UnsupportedOperationException("지원하지 않는 CODEF API 엔드포인트: " + endpoint);
        }
    }

}
