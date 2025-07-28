package com.wonnabe.auth.service;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.mapper.AuthMapper;
import com.wonnabe.auth.repository.RefreshTokenRedisRepository;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.security.account.dto.UserInfoDTO;
import com.wonnabe.common.security.service.CustomUserDetailsService;
import com.wonnabe.common.util.JsonResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.wonnabe.common.security.util.JwtProcessor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProcessor jwtProcessor;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    /**
     * 회원가입 요청을 처리합니다.
     * - 이메일 중복 여부 확인
     * - 비밀번호 암호화 및 사용자 정보 저장
     *
     * @param dto 회원가입 요청 데이터
     * @return 가입 성공 여부 (true: 성공, false: 이메일 중복)
     */
    public boolean registerUser(SignupDTO dto) {
        if (authMapper.existsByEmail(dto.getEmail()) > 0) {
            return false; // 이메일 중복
        }

        String userId = UUID.randomUUID().toString();
        String hashedPw = passwordEncoder.encode(dto.getPassword());
        authMapper.insertUserProfile(userId, dto.getName(), dto.getEmail(), hashedPw, "email");

        return true;
    }

    /**
     * 쿠키에 담긴 Refresh Token을 검증하고 새로운 Access Token을 발급합니다.
     *
     * @param request  HTTP 요청 객체 (쿠키 포함)
     * @param response HTTP 응답 객체
     * @return 새로 발급된 Access Token과 사용자 정보
     * @throws IllegalArgumentException 토큰이 존재하지 않거나 유효하지 않으면 예외 발생
     */
    public AuthResultDTO refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = extractTokenFromCookie(request, "refresh_token");
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 존재하지 않습니다.");
        }

        // 2. JWT 유효성 검사
        if (!jwtProcessor.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }

        // 3. 토큰에서 userId 추출
        String userId = jwtProcessor.getUserIdFromToken(refreshToken);

        // 4. Redis에 저장된 Refresh Token과 일치 여부 확인
        String storedRefreshToken = refreshTokenRedisRepository.get(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 만료되었거나 일치하지 않습니다.");
        }

        // 5. Access Token 재발급 및 사용자 정보 생성
        UserDetails userDetails = customUserDetailsService.loadUserByUserUUID(userId);
        String newAccessToken = jwtProcessor.generateAccessToken(userId);
        UserInfoDTO userInfo = UserInfoDTO.of(((CustomUser) userDetails).getUser());

        return new AuthResultDTO(newAccessToken, userInfo);
    }

    /**
     * 사용자의 로그아웃 요청을 처리합니다.
     * - Redis에서 Refresh Token 제거
     * - 브라우저 쿠키 제거
     *
     * @param request  HTTP 요청 객체 (쿠키 포함)
     * @param response HTTP 응답 객체
     * @throws IllegalArgumentException 토큰이 존재하지 않거나 유효하지 않으면 예외 발생
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = extractTokenFromCookie(request, "refresh_token");
        if (refreshToken == null) {
            throw new IllegalArgumentException("로그아웃 실패: Refresh Token이 존재하지 않습니다.");
        }

        // 2. JwtProcessor로부터 userId 추출
        String userId = jwtProcessor.getUserIdFromToken(refreshToken);

        // 3. Redis에서 해당 userId로 저장된 refresh token 제거
        refreshTokenRedisRepository.delete(userId);

        // 4. 클라이언트에 있는 refresh_token 쿠키 삭제
        Cookie deleteCookie = new Cookie("refresh_token", null);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(true); // HTTPS 환경에서만 전송

        response.addCookie(deleteCookie);
    }

    /**
     * 요청 쿠키에서 특정 이름의 쿠키 값을 추출합니다.
     *
     * @param request   HTTP 요청 객체
     * @param cookieKey 찾고자 하는 쿠키 이름
     * @return 쿠키 값 (없으면 null)
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieKey) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (cookieKey.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}