package com.wonnabe.auth.service;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.mapper.AuthMapper;
import com.wonnabe.auth.repository.RefreshTokenRedisRepository;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.security.service.CustomUserDetailsService;
import com.wonnabe.common.security.util.JwtProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock private AuthMapper authMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProcessor jwtProcessor;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Mock private KakaoService kakaoService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 - 성공")
    void registerUser_Success() {
        // Given
        SignupDTO dto = new SignupDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("123456");
        dto.setName("홍길동");
        dto.setSignupType("email");

        when(authMapper.existsByEmail("test@example.com")).thenReturn(0);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        // When & Then
        assertDoesNotThrow(() -> authService.registerUser(dto));

        verify(authMapper, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("123456");
        verify(authMapper, times(1)).insertUserProfile(anyString(), eq("홍길동"), eq("test@example.com"), eq("encodedPassword"), eq("email"));
    }

    @Test
    @DisplayName("회원가입 - 이미 존재하는 이메일로 예외 발생")
    void registerUser_EmailAlreadyExists() {
        // Given
        SignupDTO dto = new SignupDTO();
        dto.setEmail("existing@example.com");

        when(authMapper.existsByEmail("existing@example.com")).thenReturn(1);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.registerUser(dto));

        // 필요한 메서드만 verify
        verify(authMapper, times(1)).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("액세스 토큰 갱신 - 성공")
    void refreshAccessToken_Success() {
        // Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("refresh_token", "valid-refresh-token");

        when(req.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtProcessor.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtProcessor.getUserIdFromToken("valid-refresh-token")).thenReturn("user123");
        when(refreshTokenRedisRepository.get("user123")).thenReturn("valid-refresh-token");

        CustomUser mockUser = mock(CustomUser.class);
        UserVO mockUserVO = mock(UserVO.class);
        when(customUserDetailsService.loadUserByUserUUID("user123")).thenReturn(mockUser);
        when(jwtProcessor.generateAccessToken("user123")).thenReturn("new-access-token");
        when(mockUser.getUser()).thenReturn(mockUserVO);

        // When
        AuthResultDTO result = authService.refreshAccessToken(req, res);

        // Then
        assertThat(result, notNullValue());
        verify(jwtProcessor, times(1)).validateToken("valid-refresh-token");
        verify(jwtProcessor, times(1)).generateAccessToken("user123");
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() {
        // Given
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("refresh_token", "valid-token");

        when(req.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtProcessor.getUserIdFromToken("valid-token")).thenReturn("user123");
        when(refreshTokenRedisRepository.get("user123")).thenReturn("valid-token");

        // When & Then
        assertDoesNotThrow(() -> authService.logout(req, res));

        verify(refreshTokenRedisRepository, times(1)).delete("user123");
        verify(res, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("카카오 로그인 처리 - 기존 회원")
    void processKakaoLogin_ExistingUser() {
        // Given
        HttpServletResponse res = mock(HttpServletResponse.class);
        String code = "test-kakao-code";
        String kakaoEmail = "test@kakao.com";
        String testUserId = "test-user-id-123";

        when(kakaoService.getAccessToken(code)).thenReturn("kakao-access-token");
        when(kakaoService.getUserInfo("kakao-access-token")).thenReturn(Map.of(
                "email", kakaoEmail,
                "nickname", "카카오유저",
                "kakaoId", UUID.randomUUID().toString()
        ));
        when(authMapper.existsByEmail(kakaoEmail)).thenReturn(1); // 기존 회원

        CustomUser customUser = mock(CustomUser.class);
        UserVO mockUserVO = mock(UserVO.class);
        when(mockUserVO.getUserId()).thenReturn(testUserId); // userId 반환 설정
        when(customUserDetailsService.loadUserByUsername(kakaoEmail)).thenReturn(customUser);
        when(customUser.getUser()).thenReturn(mockUserVO);
        when(jwtProcessor.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProcessor.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        AuthResultDTO result = authService.processKakaoLogin(code, res);

        // Then
        assertThat(result, notNullValue());
        verify(kakaoService, times(1)).getAccessToken(code);
        verify(kakaoService, times(1)).getUserInfo("kakao-access-token");

        // 더 유연한 검증: anyLong()으로 long 타입 매개변수 허용
        verify(refreshTokenRedisRepository, times(1)).save(eq(testUserId), eq("refresh-token"), anyLong());
    }
}