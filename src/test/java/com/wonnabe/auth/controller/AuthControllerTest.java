package com.wonnabe.auth.controller;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.service.AuthService;
import com.wonnabe.auth.service.KakaoService;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private KakaoService kakaoService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("회원가입 - 성공")
    void signup() {
        // Given
        SignupDTO dto = new SignupDTO();
        when(authService.registerUser(dto)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> authController.signup(dto));
        verify(authService, times(1)).registerUser(dto);
    }

    @Test
    @DisplayName("액세스 토큰 갱신 - 성공")
    void refreshAccessToken() {
        // Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        AuthResultDTO mockResult = mock(AuthResultDTO.class);
        when(authService.refreshAccessToken(req, res)).thenReturn(mockResult);

        // When & Then
        assertDoesNotThrow(() -> authController.refreshAccessToken(req, res));
        verify(authService, times(1)).refreshAccessToken(req, res);
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout() {
        // Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        doNothing().when(authService).logout(req, res);

        // When & Then
        assertDoesNotThrow(() -> authController.logout(req, res));
        verify(authService, times(1)).logout(req, res);
    }

    @Test
    @DisplayName("카카오 로그인 URL 조회 - 성공")
    void getKakaoLoginUrl() {
        // Given
        String expectedUrl = "https://kakao-login-url";
        when(kakaoService.getKakaoLoginUrl()).thenReturn(expectedUrl);

        // When & Then
        assertDoesNotThrow(() -> authController.getKakaoLoginUrl());
        verify(kakaoService, times(1)).getKakaoLoginUrl();
    }

    @Test
    @DisplayName("카카오 콜백 처리 - 성공")
    void kakaoCallback() {
        // Given
        HttpServletResponse res = mock(HttpServletResponse.class);
        AuthResultDTO mockResult = mock(AuthResultDTO.class);
        when(authService.processKakaoLogin("code123", res)).thenReturn(mockResult);

        // When & Then
        assertDoesNotThrow(() -> authController.kakaoCallback("code123", null, res));
        verify(authService, times(1)).processKakaoLogin("code123", res);
    }

    @Test
    @DisplayName("카카오 로그인 - 성공")
    void kakaoLogin() {
        // Given
        HttpServletResponse res = mock(HttpServletResponse.class);
        Map<String, String> request = Map.of("code", "code123");
        AuthResultDTO mockResult = mock(AuthResultDTO.class);
        when(authService.processKakaoLogin("code123", res)).thenReturn(mockResult);

        // When & Then
        assertDoesNotThrow(() -> authController.kakaoLogin(request, res));
        verify(authService, times(1)).processKakaoLogin("code123", res);
    }
}