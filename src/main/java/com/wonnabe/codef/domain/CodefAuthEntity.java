package com.wonnabe.codef.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodefAuthEntity {
    private String userId;             // 사용자 UUID
    private String countryCode;        // KR
    private String businessType;       // BK, CD, ST, IS
    private String clientType;         // P, B, A
    private String loginType;          // 1 (ID/PW)
    private String institutionCode;    // CODEF 기관 코드
    private String loginId;            // 사용자 로그인 ID
    private String loginPassword;  // 평문 비밀번호 (전송 시 백단에서 RSA 암호화)
    private String birthDate;          // YYMMDD
    private String cardPassword;       // 앞 2자리 (RSA 전 값)
    private String institutionName;    // 기관명 (ex: KB증권 → 사용자명으로 임시 사용 가능)
    private String identity;           // 주민번호 또는 사업자번호
    private String userName;           // 사용자 이름

    private String accessToken;        // 현재 Access Token
    private LocalDateTime tokenExpiresAt; // Access Token 만료 시각
    private String connectedId;        // Connected ID
}
