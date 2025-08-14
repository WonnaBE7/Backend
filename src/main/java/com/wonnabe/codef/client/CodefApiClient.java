package com.wonnabe.codef.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.codef.dto.auth.CodefAuthParam;
import com.wonnabe.codef.dto.bank.account.BankAccountListResponse;
import com.wonnabe.codef.dto.bank.transaction.BankAccountTransactionListResponse;
import com.wonnabe.codef.dto.bank.savings.BankSavingsTransactionListResponse;
import com.wonnabe.codef.dto.card.account.CardListResponse;
import com.wonnabe.codef.dto.card.transaction.CardTransactionListResponse;
import com.wonnabe.codef.dto.invest.account.InvestAccountListResponse;
import com.wonnabe.codef.util.AssetRequestBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class CodefApiClient {

    private final RestTemplate rtFast;
    private final RestTemplate rtSlow;
    private final ObjectMapper objectMapper;
    private final AssetRequestBuilder assetRequestBuilder;

    @Autowired
    public CodefApiClient(@Qualifier("restTemplateFast") RestTemplate rtFast,
                          @Qualifier("restTemplateSlow") RestTemplate rtSlow,
                          ObjectMapper objectMapper,
                          AssetRequestBuilder assetRequestBuilder) {
        this.rtFast = rtFast;
        this.rtSlow = rtSlow;
        this.objectMapper = objectMapper;
        this.assetRequestBuilder = assetRequestBuilder;
    }

    public Object fetchRawAccountResponse(CodefAuthParam param) {
        String url = param.getEndpoint();

        try {
            Map<String, Object> body = assetRequestBuilder.buildAssetCreateRequest(param);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(param.getAccessToken());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate rt = pickTemplate(url);
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, request, String.class);

            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new CodefRemoteException("Non-2xx status: " + res.getStatusCodeValue());
            }
            String raw = res.getBody();
            if (raw == null || raw.isBlank()) {
                throw new CodefRemoteException("Empty body from CODEF");
            }

            String normalized = maybeUrlDecodeIfNeeded(raw);

            try {
                JsonNode probe = objectMapper.readTree(normalized);
                log.debug("CODEF response OK ({}): {}", url, truncate(normalized, 200));
            } catch (Exception ignore) {
                log.warn("Response is not valid JSON before parse ({}): {}", url, truncate(normalized, 200));
            }

            return parseApiResponse(url, normalized);
        } catch (ResourceAccessException e) {
            // 네트워크/타임아웃(Connect/Read 등)
            log.error("CODEF 네트워크 오류 - 기관: {}, endpoint: {}, msg: {}", param.getInstitutionCode(), url, e.getMessage());
            throw new CodefTimeoutException("CODEF timeout/network error", e);

        } catch (HttpStatusCodeException e) {
            // 4xx/5xx 응답
            log.error("CODEF 비정상 상태코드 - 기관: {}, endpoint: {}, status: {}, body: {}",
                    param.getInstitutionCode(), url, e.getStatusCode(), truncate(e.getResponseBodyAsString(), 300));
            throw new CodefRemoteException("CODEF returned " + e.getRawStatusCode(), e);

        } catch (RestClientException e) {
            // 그 외 RestTemplate 계열
            log.error("CODEF 호출 실패 - 기관: {}, endpoint: {}, msg: {}", param.getInstitutionCode(), url, e.getMessage());
            throw new CodefRemoteException("CODEF client error", e);

        } catch (Exception e) {
            // 파싱/로직 오류
            log.error("CODEF 응답 파싱 실패 - 기관: {}, endpoint: {}, msg: {}", param.getInstitutionCode(), url, e.getMessage());
            throw new CodefParsingException("CODEF parsing failed", e);
        }
    }

    private RestTemplate pickTemplate(String endpoint) {
        if (endpoint.contains("/approval-list")
                || endpoint.contains("/transaction-list")) {
            return rtSlow;
        }
        return rtFast;
    }

    private String maybeUrlDecodeIfNeeded(String raw) {
        String t = raw.trim();
        if (t.startsWith("{") || t.startsWith("[")) return raw;
        if (t.contains("%7B") || t.contains("%7D") || t.contains("%22") || t.contains("%5B") || t.contains("%5D")) {
            try { return URLDecoder.decode(raw, StandardCharsets.UTF_8.name()); } catch (Exception ignore) {}
        }
        return raw;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private Object parseApiResponse(String endpoint, String rawJson) throws Exception {
        if (endpoint.contains("/card/p/account/card-list")) {
            // 카드 보유카드 조회
            return objectMapper.readValue(rawJson, CardListResponse.class);
        } else if (endpoint.contains("/card/p/account/approval-list")) {
            // 카드 승인내역 조회
            return objectMapper.readValue(rawJson, CardTransactionListResponse.class);
        } else if (endpoint.contains("/bank/p/account/account-list")) {
            // 은행 보유계좌 조회
            return objectMapper.readValue(rawJson, BankAccountListResponse.class);
        } else if (endpoint.contains("/bank/p/account/transaction-list")) {
            // 은행 수시입출 거래내역 조회
            return objectMapper.readValue(rawJson, BankAccountTransactionListResponse.class);
        } else if (endpoint.contains("/bank/p/installment-savings/transaction-list")) {
            // 은행 적금 거래내역 조회
            return objectMapper.readValue(rawJson, BankSavingsTransactionListResponse.class);
        } else if (endpoint.contains("/stock/a/account/account-list")) {
            // 증권 보유계좌 조회
            return objectMapper.readValue(rawJson, InvestAccountListResponse.class);
        } else {
            throw new UnsupportedOperationException("지원하지 않는 CODEF API 엔드포인트: " + endpoint);
        }
    }

    public static class CodefTimeoutException extends RuntimeException {
        public CodefTimeoutException(String msg, Throwable cause) { super(msg, cause); }
    }

    public static class CodefRemoteException extends RuntimeException {
        public CodefRemoteException(String msg) { super(msg); }
        public CodefRemoteException(String msg, Throwable cause) { super(msg, cause); }
    }

    public static class CodefParsingException extends RuntimeException {
        public CodefParsingException(String msg, Throwable cause) { super(msg, cause); }
    }

}
