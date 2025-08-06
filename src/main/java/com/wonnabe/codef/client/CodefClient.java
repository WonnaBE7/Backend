package com.wonnabe.codef.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.codef.dto.ConnectedIdResponseDto;
import com.wonnabe.codef.dto.AccessTokenResponseDto;
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

    public AccessTokenResponseDto requestAccessToken() {
        // 1. clientId, clientSecret 인코딩
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + auth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "read");

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        ResponseEntity<AccessTokenResponseDto> response = restTemplate.exchange(
                "https://oauth.codef.io/oauth/token",
                HttpMethod.POST,
                request,
                AccessTokenResponseDto.class
        );

        return response.getBody();
    }

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

    public ConnectedIdResponseDto requestConnectedId(String accessToken, Map<String, Object> accountMap) {
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
            return mapper.readValue(raw, ConnectedIdResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Connected ID 발급 실패 (Map 기반)", e);
        }
    }

    // + API 호출 실패 시 재시도 / 예외처리 등 유틸 함수 포함
}

