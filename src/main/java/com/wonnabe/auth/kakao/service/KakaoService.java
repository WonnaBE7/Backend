// getKakaoLoginUrl() - 카카오 로그인 창으로 리디렉트할 URL 생성
// getKakaoUserInfo(code) - 인가코드 → 액세스 토큰 요청 → 사용자 정보 조회
// getAccessToken(code) - 인가코드로 access_token 요청 (POST)
// getUserInfo(accessToken) - access_token으로 사용자 정보 요청 (GET)

package com.wonnabe.auth.kakao.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.auth.kakao.dto.KakaoTokenResponse;
import com.wonnabe.auth.dto.KakaoUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService {

    // .properties 파일에 저장된 카카오 REST API 앱 정보를 주입받음
    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

//    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate(); // 외부 API 호출용 객체 RestTemplate

    // 1. 로그인 URL 생성
    // 카카오 로그인 창 URL 생성 -> 프론트에서 이 URL로 redirect하면 사용자에게 로그인 창이 뜸
    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri;
    }

    // 2. 인가 코드 -> 사용자 정보까지 얻기
    // code를 받아 token 요청 & 사용자 정보 요청까지 처리 -> 컨트롤러에서 이 메서드 하나만 호출하면 됨
    public KakaoUserInfoDTO getKakaoUserInfo(String code) {
        KakaoTokenResponse tokenResponse = getAccessToken(code);
        return getUserInfo(tokenResponse.getAccess_token());
    }

    // 3. 카카오 토큰 요청
    // 카카오에서 code를 받고 → access_token을 발급받기 위한 POST 요청 URL
    private KakaoTokenResponse getAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";
        // 카카오가 요구하는 content type 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 카카오가 요구하는 필수 파라미터들 구성
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientId);
        params.put("redirect_uri", redirectUri);
        params.put("code", code);
        // POST 요청 보내고 -> access_token, refresh_token 등이 담긴 응답 받아옴
        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(url, request, KakaoTokenResponse.class);

        return response.getBody();
    }

    // 4. accessToken으로 사용자 정보 요청
    // 이 URL은 카카오에서 사용자 정보(닉네임, 이메일 등)를 가져올 때 사용하는 엔드포인트
    private KakaoUserInfoDTO getUserInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";
        // Authorization: Bearer {accessToken} 방식으로 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // GET 방식으로 요청을 보내고 → 응답으로 사용자 정보를 파싱함
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<KakaoUserInfoDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, KakaoUserInfoDTO.class);

        return response.getBody();
    }
}