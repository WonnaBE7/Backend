package com.wonnabe.auth.util;

import org.springframework.stereotype.Component;

/**
 * JWT 처리 유틸리티 클래스
 * 현재는 기본 구조만, 팀원이 구현 완료하면 실제 로직 추가
 */

@Component
public class JwtUtil {
    /**
     * JWT 토큰에서 사용자 ID 추출
     * TODO: 팀원이 JWT 구현 완료 시 실제 로직 구현
     */
    public String extractUserId(String token) {
        // 임시 구현 - 팀원이 JWT 구현 완료 시 교체
        throw new RuntimeException("JWT 기능이 아직 구현되지 않았습니다.");
    }

    /**
     * JWT 토큰 유효성 검증
     * TODO: 팀원이 JWT 구현 완료 시 실제 로직 구현
     */
    public boolean validateToken(String token) {
        // 임시 구현
        return false;
    }
}
