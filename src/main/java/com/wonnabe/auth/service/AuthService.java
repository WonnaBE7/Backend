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

// 회원가입 로직을 처리하는 핵심 서비스
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

    // 회원가입 (이메일 중복 체크 / 비밀번호 해싱 / DB insert)
    // Vue에서 전송해온 SignupDTO(이름, 이메일, 비밀번호)
    public boolean registerUser(SignupDTO dto) {

        // 1. 이메일 중복 체크
        if (authMapper.existsByEmail(dto.getEmail()) > 0) {
            return false;
        }

        // 2. 비밀번호 해싱
        String userId = UUID.randomUUID().toString(); // 회원의 고유 식별자(userId) 랜덤 생성
        String hashedPw = passwordEncoder.encode(dto.getPassword()); // 비밀번호를 단방향 암호화함(BCrypt)

        // 3. 회원정보 DB에 저장
        authMapper.insertUserProfile(userId, dto.getName(), dto.getEmail(), hashedPw, "email"); // signupType = "email"로 저장해서 소셜로그인과 구분 가능하도록 설계
        return true;
    }

    // 카카오 로그인을 시도한 사용자가 DB에 있으면 가져오고, 없으면 회원가입시켜주는 메서드
    public User findOrCreateKakaoUser(KakaoUserInfoDTO kakaoUser) {
        // 카카오가 준 응답에서 이메일과 닉네임 꺼냄
        String email = kakaoUser.getKakao_account().getEmail();
        String nickname = kakaoUser.getKakao_account().getProfile().getNickname();
        // 이미 우리 DB에 가입된 유저인지 확인. 있으면 그냥 그 사용자 객체 리턴
        User user = userMapper.findByEmail(email);
        if (user != null) return user;
        // 새 유저를 만들기 위해 고유한 userId 생성
        String userId = UUID.randomUUID().toString();

        // 새로운 카카오 사용자 생성
        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setEmail(email);
        newUser.setName(nickname);
        newUser.setSignupType("kakao"); // 소셜 로그인 구분용
        // DB에 저장하고 리턴
        userMapper.insertKakaoUser(newUser);
        return newUser;
    }

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    // User 객체를 기반으로 accessToken과 refreshToken을 발급해주는 메서드
    // (로그인 or 카카오 로그인 성공 후 Access + Refresh 둘 다 발급 + Redis 저장)
    public TokenResponseDTO login(User user) {
        // JWT Processor가 토큰을 생성해줌 (AccessToken 1hr & Refresh Token 7d)
        String accessToken = jwtProcessor.generateAccessToken(user.getUserId());
        String refreshToken = jwtProcessor.generateRefreshToken();

        // RefreshToken Redis에 저장
        // (나중에 토큰 재발급 요청 왔을 때 여기서 꺼내서 비교할 수 있도록)
        refreshTokenRedisRepository.save(user.getUserId(), refreshToken, refreshTokenExpirationMs);

        // access + refresh token을 DTO로 포장해서 응답할 수 있게 함
        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // AccessToken이 만료됐을 때, 쿠키에 있는 Refresh Token으로 새로 발급해주는 메서드
    // (AccessToken 만료 후 /api/auth/refresh 호출 시 AccessToken만 새로 발급)
    public ResponseEntity<Object> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {

        // 1. 쿠키에서 Refresh Token 추출
        Cookie[] cookies = request.getCookies();
        // 쿠키가 하나도 없다면 로그인 정보 자체가 없는 것 -> 401 Unauthorized 응답 (인증 필요)
        if (cookies == null) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다.");
        }

        // 쿠키 배열 돌면서 이름이 refresh_token인 쿠키 찾아냄. 찾으면 그 값이 JWT 문자열
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        // 못 찾았으면 역시 인증 실패!
        if (refreshToken == null) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다.");
        }

        // 2. JWT 유효성 검사 (그 토큰이 진짜 유효한지 확인)
        // Refresh Token이 위조되지 않았는지, 만료되지는 않았는지 검증하는 과정. 서명 위조나 유효시간 초과된 토큰 걸러짐
        if (!jwtProcessor.validateToken(refreshToken)) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 유효하지 않습니다.");
        }

        // 3. userId 추출 & refresh token 비교 (그 토큰이 서버Redis에 저장된 것과 일치하는지 확인)
        // jwt 안에는 누구의 토큰인지(userId) 정보가 담겨 있어서 이걸 꺼내서 이 사용자가 누구인지 확인해야 함
        String userId = jwtProcessor.getUserIdFromToken(refreshToken);

        // Redis 저장된 토큰 조회 (사용자가 최근에 로그인했을 때 발급한 refreshToken이 Redis에 저장되어 있음)
        String storedRefreshToken = refreshTokenRedisRepository.get(userId);
        // 만약 Redis에 없거나, 값이 다르면? → 토큰 탈취거나 세션 만료 => 그래서 인증 실패!
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            return JsonResponse.error(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었거나 일치하지 않습니다.");
        }

        // 5. 맞다면 -> 새로운 Access Token 생성
        // 새 accessToken 발급 -> 사용자는 다시 로그인하지 않아도 API 요청을 이어갈 수 있음
        UserDetails userDetails = customUserDetailsService.loadUserByUserUUID(userId);
        String newAccessToken = jwtProcessor.generateAccessToken(userId);
        // 응답에 새로운 accessToken + 사용자 정보 담아서 프론트로 보내줌
        UserInfoDTO userInfo = UserInfoDTO.of(((CustomUser) userDetails).getUser());
        AuthResultDTO result = new AuthResultDTO(newAccessToken, userInfo);
        return JsonResponse.ok("Access Token 재발급 완료", result);
    }

}