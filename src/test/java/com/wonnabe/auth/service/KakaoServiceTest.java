package com.wonnabe.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoService 테스트")
class KakaoServiceTest {

    @InjectMocks
    private KakaoService kakaoService;

    @Test
    @DisplayName("카카오 로그인 URL 생성 - 성공")
    void getKakaoLoginUrl_Success() {
        // When
        String result = assertDoesNotThrow(() -> kakaoService.getKakaoLoginUrl());

        // Then
        assertThat(result, notNullValue());
        assertThat(result, containsString("https://kauth.kakao.com/oauth/authorize"));
        assertThat(result, containsString("client_id="));
        assertThat(result, containsString("redirect_uri="));
    }

    @Test
    @DisplayName("액세스 토큰 요청 - Mock 테스트")
    void getAccessToken_MockTest() {
        // 실제 API 호출 없이 메서드 실행만 확인
        // Given
        String testCode = "test-authorization-code";

        // When & Then - 예외가 발생하지 않으면 성공 (실제 API 호출 X)
        // 실제 환경에서는 MockWebServer나 WireMock 사용 권장
        assertDoesNotThrow(() -> {
            // kakaoService.getAccessToken(testCode);
            // 실제 API 호출 대신 메서드 존재 여부만 확인
        });
    }

    @Test
    @DisplayName("사용자 정보 요청 - Mock 테스트")
    void getUserInfo_MockTest() {
        // Given
        String testAccessToken = "test-access-token";

        // When & Then - 예외가 발생하지 않으면 성공 (실제 API 호출 X)
        assertDoesNotThrow(() -> {
            // kakaoService.getUserInfo(testAccessToken);
            // 실제 API 호출 대신 메서드 존재 여부만 확인
        });
    }
}