//package com.wonnabe.auth.service;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//
//@Service
//public class JwtService {
//
//    private final String SECRET_KEY = "my-secret-key"; // 실제 서비스에선 .env나 properties에서 불러오기
//    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1시간
//    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일
//
//    public String createAccessToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//
//    public String createRefreshToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//}
