package com.wonnabe.asset.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodefAuthEntity {

    private Long id;                          // PK
    private String userId;                    // 사용자 UUID
    private String institutionCode;           // 기관 코드
    private String loginId;                   // 로그인 ID
    private String loginPassword;             // 원본 비밀번호 (연습용)
    private String connectedId;               // Connected ID
    private String accessToken;               // Access Token
    private LocalDateTime tokenExpiresAt;     // 토큰 만료 시각
    private LocalDateTime createdAt;          // 생성일시
    private LocalDateTime updatedAt;          // 수정일시
    private String institutionName;           // 기관명 (옵션)

}
