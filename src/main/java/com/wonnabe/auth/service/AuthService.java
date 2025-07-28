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
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    public boolean registerUser(SignupDTO dto) {
        if (authMapper.existsByEmail(dto.getEmail()) > 0) {
            return false; // 이메일 중복
        }
        String userId = UUID.randomUUID().toString();
        String hashedPw = passwordEncoder.encode(dto.getPassword());
        authMapper.insertUserProfile(userId, dto.getName(), dto.getEmail(), hashedPw, "email");
        return true;
    }

    public ResponseEntity<Object> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다.");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다.");
        }

        // 2. JWT 유효성 검사
        if (!jwtProcessor.validateToken(refreshToken)) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 유효하지 않습니다.");
        }

        // 3. userId 추출
        String userId = jwtProcessor.getUserIdFromToken(refreshToken);

        // 4. Redis 저장된 토큰 조회
        String storedRefreshToken = refreshTokenRedisRepository.get(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었거나 일치하지 않습니다.");
        }

        // 5. 새로운 Access Token 생성
        UserDetails userDetails = customUserDetailsService.loadUserByUserUUID(userId);
        String newAccessToken = jwtProcessor.generateAccessToken(userId);
        UserInfoDTO userInfo = UserInfoDTO.of(((CustomUser) userDetails).getUser());

        AuthResultDTO result = new AuthResultDTO(newAccessToken, userInfo);
        return JsonResponse.ok("Access Token 재발급 완료", result);
    }

    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 refresh_token 추출
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, "로그아웃 실패: 쿠키가 존재하지 않습니다.");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, "로그아웃 실패: Refresh Token이 존재하지 않습니다.");
        }

        // 2. JwtProcessor로부터 userId 추출
        String userId = jwtProcessor.getUserIdFromToken(refreshToken);

        // 3. Redis에서 해당 userId로 저장된 refresh token 제거
        refreshTokenRedisRepository.delete(userId);

        // 4. 클라이언트에 있는 refresh_token 쿠키 삭제 (Max-Age: 0)
        Cookie deleteCookie = new Cookie("refresh_token", null);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        deleteCookie.setHttpOnly(true); // 보안을 위해 동일 옵션 설정
        deleteCookie.setSecure(true);   // HTTPS 환경이라면 true
        response.addCookie(deleteCookie);

        // 5. 성공 응답 반환
        return JsonResponse.ok("로그아웃이 완료되었습니다.");
    }

}