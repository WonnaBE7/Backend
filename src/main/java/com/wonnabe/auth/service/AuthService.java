package com.wonnabe.auth.service;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.mapper.AuthMapper;
import com.wonnabe.auth.repository.RefreshTokenRedisRepository;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.dto.AuthResultDTO;
import com.wonnabe.common.security.account.dto.UserInfoDTO;
import com.wonnabe.common.security.service.CustomUserDetailsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.wonnabe.common.security.util.JwtProcessor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Service
@Log4j2
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

    @Autowired
    private KakaoService kakaoService;

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
        authMapper.insertUserProfile(userId, dto.getName(), dto.getEmail(), hashedPw, dto.getSignupType());

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

    /**
     * 카카오 로그인 처리 (완전 수정된 버전)
     * 기존 회원가입/로그인 로직을 최대한 재사용
     */
    public AuthResultDTO processKakaoLogin(String code, HttpServletResponse response) {
        try {
            // 1. 카카오에서 사용자 정보 받아오기
            String accessToken = kakaoService.getAccessToken(code);
            Map<String, String> kakaoUserInfo = kakaoService.getUserInfo(accessToken);

            String email = kakaoUserInfo.get("email");
            String nickname = kakaoUserInfo.get("nickname");
            String kakaoId = kakaoUserInfo.get("kakaoId");

            // 2. 이메일이 없는 경우 임시 이메일 생성 (수정된 부분!)
            if (email == null || email.isEmpty()) {
                email = kakaoId + "@kakao.temp";  // 4357475616@kakao.temp
                log.info("카카오 이메일 권한 없음. 임시 이메일 사용: {}", email);
            }

            // 3. 기존 회원 확인
            boolean userExists = authMapper.existsByEmail(email) > 0;
            log.info("사용자 존재 여부 확인: email={}, exists={}", email, userExists);

            if (!userExists) {
                // 4. 신규 회원 - 기존 회원가입 로직 활용
                SignupDTO kakaoSignup = new SignupDTO();
                kakaoSignup.setEmail(email);
                kakaoSignup.setName(nickname);
                kakaoSignup.setPassword(UUID.randomUUID().toString().substring(0, 16));
                kakaoSignup.setSignupType("kakao");

                log.info("카카오 신규 회원가입 시도: email={}, name={}", email, nickname);

                // 기존 회원가입 메서드 재사용
                boolean signupResult = registerUser(kakaoSignup);
                if (!signupResult) {
                    log.error("카카오 회원가입 실패: {}", email);
                    throw new RuntimeException("카카오 회원가입 처리 실패");
                }

                log.info("카카오 신규 회원 가입 완료: {}", email);
            } else {
                log.info("카카오 기존 회원 로그인: {}", email);
            }

            // 5. 사용자 정보 조회 (기존 로직 재사용)
            log.info("사용자 정보 조회 시도: {}", email);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            log.info("사용자 정보 조회 성공: {}", email);

            CustomUser customUser = (CustomUser) userDetails;
            String userId = customUser.getUser().getUserId();

            // 6. JWT 토큰 발급
            log.info("JWT 토큰 발급 시작: userId={}", userId);
            String jwtAccessToken = jwtProcessor.generateAccessToken(userId);
            String refreshToken = jwtProcessor.generateRefreshToken(userId);

            // 7. 토큰 저장 및 쿠키 설정
            log.info("Redis 토큰 저장 및 쿠키 설정 시작");

            // Redis 저장 시도 (실패해도 로그인은 성공 처리)
            try {
                refreshTokenRedisRepository.save(userId, refreshToken, 60 * 60 * 24 * 7);
                log.info("Redis 토큰 저장 성공");
            } catch (Exception redisException) {
                log.warn("Redis 저장 실패, 쿠키만 설정: {}", redisException.getMessage());
                // Redis 실패해도 계속 진행 (쿠키는 설정됨)
            }

            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24 * 7);
            response.addCookie(refreshCookie);

            // 8. 응답 생성
            log.info("응답 생성 시작");
            UserInfoDTO userInfo = UserInfoDTO.of(customUser.getUser());
            AuthResultDTO result = new AuthResultDTO(jwtAccessToken, userInfo);

            log.info("카카오 로그인 처리 완료: userId={}", userId);
            return result;

        } catch (Exception e) {
            log.error("카카오 로그인 처리 실패", e);
            throw new RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}