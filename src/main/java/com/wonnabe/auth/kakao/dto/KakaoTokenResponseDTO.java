package com.wonnabe.auth.kakao.dto;

import lombok.Data;

@Data
public class KakaoTokenResponse {
    private String token_type;
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private int refresh_token_expires_in;
    private String scope;
}

// access_token	우리가 이후 요청에 쓸 인증 토큰
// refresh_token	장기 인증 토큰 (선택적 사용)
// expires_in	access_token 유효기간(초)
// token_type	일반적으로 “bearer”
// scope	동의한 항목들 (예: profile, account_email 등)