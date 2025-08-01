package com.wonnabe.common.security.account.domain;

import lombok.Data;

@Data
public class UserVO {
    private String userId;
    private String name;
    private String email;
    private String passwordHash; // 비밀번호 컬럼명과 일치
}
