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

    /**
     * 주어진 파라미터로 CODEF 자산 API를 호출하고, 응답을 해당 DTO로 파싱해 반환합니다.
     * 1) 요청 바디를 생성하고 Bearer 토큰과 함께 POST 호출
     * 2) 응답 바디가 URL-encoded인 경우 디코딩
     * 3) 엔드포인트 경로에 따라 적절한 DTO 클래스로 역직렬화
     *
     * @param param 엔드포인트/인증토큰/기관코드 등 호출 파라미터
     * @return 엔드포인트에 대응하는 DTO 객체
     *         (CardListResponse, CardTransactionListResponse, BankAccountListResponse,
     *          BankAccountTransactionListResponse, BankSavingsTransactionListResponse,
     *          InvestAccountListResponse 중 하나)
     *
     * @throws CodefTimeoutException 네트워크 타임아웃/접근 오류(ResourceAccessException)
     * @throws CodefRemoteException  HTTP 4xx/5xx, 빈 바디, 비정상 상태 등 원격 오류
     * @throws CodefParsingException 응답 파싱 실패 등 로직/포맷 오류
     * @throws UnsupportedOperationException 지원하지 않는 엔드포인트 유형
     */
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

    /**
     * 엔드포인트 경로에 따라 사용할 RestTemplate을 선택합니다.
     * - 거래내역/승인내역처럼 응답이 무거운 API는 {@code rtSlow} 선택
     * - 그 외는 {@code rtFast} 선택
     *
     * @param endpoint 호출 대상 엔드포인트 URL
     * @return 선택된 RestTemplate
     */
    private RestTemplate pickTemplate(String endpoint) {
        if (endpoint.contains("/approval-list")
                || endpoint.contains("/transaction-list")) {
            return rtSlow;
        }
        return rtFast;
    }

    /**
     * 응답 문자열이 URL-encoded로 보이면 UTF-8로 디코딩해 반환합니다.
     * 이미 JSON 포맷({ 또는 [ 로 시작)으로 보이면 원본을 그대로 반환합니다.
     *
     * @param raw 원본 응답 문자열
     * @return 필요한 경우 디코딩된 문자열, 아니면 원본
     */
    private String maybeUrlDecodeIfNeeded(String raw) {
        String t = raw.trim();
        if (t.startsWith("{") || t.startsWith("[")) return raw;
        if (t.contains("%7B") || t.contains("%7D") || t.contains("%22") || t.contains("%5B") || t.contains("%5D")) {
            try { return URLDecoder.decode(raw, StandardCharsets.UTF_8.name()); } catch (Exception ignore) {}
        }
        return raw;
    }

    /**
     * 로그 출력을 위한 안전한 문자열 트렁케이션을 수행합니다.
     *
     * @param s   원본 문자열
     * @param max 최대 길이
     * @return 잘린 문자열(넘치면 "..." 추가), s가 null이면 null
     */
    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /**
     * 엔드포인트 경로에 따라 적절한 DTO 클래스로 JSON을 역직렬화합니다.
     * 지원하지 않는 엔드포인트이면 {@link UnsupportedOperationException} 을 던집니다.
     *
     * @param endpoint 호출한 엔드포인트 URL
     * @param rawJson  정규화된 응답 JSON 문자열
     * @return 역직렬화된 DTO 객체
     * @throws Exception JSON 파싱 오류 등 역직렬화 실패 시
     */
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

    /** CODEF 호출 중 네트워크 타임아웃/접근 오류를 나타내는 예외 */
    public static class CodefTimeoutException extends RuntimeException {
        public CodefTimeoutException(String msg, Throwable cause) { super(msg, cause); }
    }

    /** CODEF로부터의 비정상 응답(4xx/5xx/빈바디 등)을 나타내는 예외 */
    public static class CodefRemoteException extends RuntimeException {
        public CodefRemoteException(String msg) { super(msg); }
        public CodefRemoteException(String msg, Throwable cause) { super(msg, cause); }
    }

    /** 응답 파싱/로직 처리 중 발생한 오류를 나타내는 예외 */
    public static class CodefParsingException extends RuntimeException {
        public CodefParsingException(String msg, Throwable cause) { super(msg, cause); }
    }

}
