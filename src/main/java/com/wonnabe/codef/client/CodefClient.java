package com.wonnabe.codef.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.codef.dto.auth.ConnectedIdResponse;
import com.wonnabe.codef.dto.auth.AccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CodefClient {

    @Value("${codef.client-id}")
    private String clientId;

    @Value("${codef.client-secret}")
    private String clientSecret;

    @Value("${codef.public-key}")
    private String publicKey;

    private final RestTemplate restTemplate;

    /**
     * CODEF OAuth 서버에 Client Credentials 방식으로 Access Token을 요청합니다.
     * Authorization 헤더는 Basic (base64(clientId:clientSecret))을 사용하며,
     * grant_type=client_credentials, scope=read 로 호출합니다.
     *
     * @return 발급된 액세스 토큰 응답 객체
     * @throws org.springframework.web.client.RestClientException 외부 호출 실패 시(네트워크/4xx/5xx 등)
     */
    public AccessTokenResponse requestAccessToken() {
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + auth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "read");

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                "https://oauth.codef.io/oauth/token",
                HttpMethod.POST,
                request,
                AccessTokenResponse.class
        );
        return response.getBody();
    }

    /**
     * RSA 공개키(PEM)로 비밀번호를 암호화하여 Base64 문자열로 반환합니다.
     * - 키 포맷: X.509 (-----BEGIN PUBLIC KEY-----/END-----)
     * - 알고리즘: RSA/ECB/PKCS1Padding
     *
     * @param password 평문 비밀번호
     * @return RSA로 암호화된 비밀번호(Base64 인코딩)
     * @throws RuntimeException 공개키 파싱/암호화 과정에서 오류가 발생한 경우
     */
    public String encryptPassword(String password) {
        try {
            String publicKeyPEM = publicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("비밀번호 암호화 실패", e);
        }
    }

    /**
     * CODEF 계정 등록 API를 호출하여 Connected ID를 발급받습니다.
     * 요청 본문은 {"accountList":[{...}]} 형태로 전달되며,
     * 응답 본문은 URL-encoded 문자열일 수 있어 디코딩 후 JSON 파싱합니다.
     *
     * @param accessToken   Bearer 액세스 토큰
     * @param accountMap    계정 등록 파라미터 맵(organization, id, password 등)
     * @return Connected ID 응답 객체
     * @throws RuntimeException 외부 호출/디코딩/파싱 실패 시
     */
    public ConnectedIdResponse requestConnectedId(String accessToken, Map<String, Object> accountMap) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("accountList", Collections.singletonList(accountMap));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<?> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://development.codef.io/v1/account/create",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            String raw = URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8.name());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(raw, ConnectedIdResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Connected ID 발급 실패 (Map 기반)", e);
        }
    }
}

