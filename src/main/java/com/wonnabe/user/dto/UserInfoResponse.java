package com.wonnabe.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoResponse {
    private final String userId;
    private final String name;
    private final String email;

    @Builder
    public UserInfoResponse(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}
