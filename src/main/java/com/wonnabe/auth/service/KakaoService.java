// com.wonnabe.auth.service.KakaoService.java
package com.wonnabe.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 카카오 OAuth API 전용 서비스
 * RestTemplate을 사용하여 카카오 API 호출
 */
@Service
@Log4j2
public class KakaoService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KakaoService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 카카오 로그인 URL 생성
     */
    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code";
    }

    /**
     * 인증 코드로 액세스 토큰 받기
     */
    public String getAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // JSON에서 access_token 추출
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            log.info("카카오 액세스 토큰 발급 성공");
            return accessToken;

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 실패", e);
            throw new RuntimeException("카카오 액세스 토큰 요청 실패", e);
        }
    }

    /**
     * 액세스 토큰으로 사용자 정보 조회
     * 우리 시스템에서 필요한 형태로 단순화해서 반환
     */
    public Map<String, String> getUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, String> userInfo = new HashMap<>();

            // 카카오 ID
            userInfo.put("kakaoId", jsonNode.get("id").asText());

            // 이메일 (kakao_account.email)
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.has("email")) {
                userInfo.put("email", kakaoAccount.get("email").asText());
            }

            // 닉네임 (kakao_account.profile.nickname 또는 properties.nickname)
            String nickname = null;
            if (kakaoAccount != null && kakaoAccount.has("profile")) {
                JsonNode profile = kakaoAccount.get("profile");
                if (profile.has("nickname")) {
                    nickname = profile.get("nickname").asText();
                }
            }
            if (nickname == null && jsonNode.has("properties")) {
                JsonNode properties = jsonNode.get("properties");
                if (properties.has("nickname")) {
                    nickname = properties.get("nickname").asText();
                }
            }
            userInfo.put("nickname", nickname != null ? nickname : "카카오사용자");

            log.info("카카오 사용자 정보 조회 성공: {}", userInfo.get("email"));
            return userInfo;

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }
}