package com.wonnabe.common.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResultDTO {

    private String accessToken;
    private UserInfoDTO user;
}
