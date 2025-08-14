package com.wonnabe.codef.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccessTokenResponse {

    private String accessToken;
    private String expiresIn;
    private String tokenType;
    private String scope;
}
