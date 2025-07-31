package com.wonnabe.asset.client;

import com.wonnabe.asset.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CodefAuthClient {

    @Value("${codef.client-id}")
    private String clientId;

    @Value("${codef.client-secret}")
    private String clientSecret;

    @Value("${codef.public-key}")
    private String publicKey;

    private final RestTemplate restTemplate;

    public TokenResponseDto requestAccessToken() {
        // 1. clientId, clientSecret 인코딩
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + auth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "read");

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponseDto> response = restTemplate.exchange(
                "https://oauth.codef.io/oauth/token",
                HttpMethod.POST,
                request,
                TokenResponseDto.class
        );

        return response.getBody();
    }

//    public String encryptPassword(String password) {
//        // RSA 공개키로 암호화
//        // ... your implementation here ...
//    }
//
//    public ConnectedIdResponse createConnectedId(String accessToken, AccountRequestDto accountDto) {
//        // 계정 생성 API 호출
//        // ... your implementation here ...
//    }

    // + API 호출 실패 시 재시도 / 예외처리 등 유틸 함수 포함
}

