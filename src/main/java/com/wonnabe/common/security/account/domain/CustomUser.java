package com.wonnabe.common.security.account.domain;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

/**
 * Spring Security의 User를 확장하여
 * 애플리케이션 도메인 사용자 정보(UserVO)를 포함하는 사용자 구현체입니다.
 */
@Getter
public class CustomUser extends User {

    private final UserVO user;

    /**
     * CustomUser 객체를 생성합니다.
     * @param user 애플리케이션 도메인 사용자 정보(UserVO)
     */
    public CustomUser(UserVO user) {
        super(user.getEmail(), user.getPasswordHash(), Collections.emptyList());
        this.user = user;
    }
}
