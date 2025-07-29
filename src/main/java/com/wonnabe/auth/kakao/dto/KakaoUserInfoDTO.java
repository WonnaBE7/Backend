package com.wonnabe.auth.dto;

import lombok.Data;

@Data
public class KakaoUserInfoDTO {
    private Long id; // 카카오 유저 고유 ID
    private String connected_at;
    private KakaoAccount kakao_account;

    @Data
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Data
        public static class Profile {
            private String nickname;
        }
    }
}