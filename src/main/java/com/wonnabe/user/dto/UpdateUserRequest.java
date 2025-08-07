package com.wonnabe.user.dto;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
    private String name;
    private String password;
}