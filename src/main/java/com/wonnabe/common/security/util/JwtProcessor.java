package com.wonnabe.common.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Log4j2
@Component
public class JwtProcessor {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;

    // 토큰 유효시간 설정 (ms)
    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 15;      // 15분
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7일

    /**
     * secretKey를 기반으로 JWT 서명 키를 초기화합니다.
     * Spring 초기화 이후 자동 실행됨 (@PostConstruct)
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 주어진 사용자 ID를 기반으로 Access Token을 생성합니다.
     *
     * @param userId 사용자 고유 ID
     * @return 생성된 Access Token (15분 유효)
     */
    public String generateAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 주어진 사용자 ID를 기반으로 Refresh Token을 생성합니다.
     *
     * @param userId 사용자 고유 ID
     * @return 생성된 Refresh Token (7일 유효)
     */
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID(subject)를 추출합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 사용자 ID (subject 클레임 값)
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료됨: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 유효하지 않음: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰의 만료 일자를 반환합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 만료 시각 (Date 객체)
     */
    public Date getExpirationDate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}