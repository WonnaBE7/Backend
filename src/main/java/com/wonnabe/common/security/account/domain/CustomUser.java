// com.wonnabe.common.security.account.domain.CustomUser.java
package com.wonnabe.common.security.account.domain;

import com.wonnabe.common.security.account.domain.UserVO;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class CustomUser extends User {

    private final UserVO user;

    public CustomUser(UserVO user) {
        super(user.getEmail(), user.getPasswordHash(), Collections.emptyList());
        this.user = user;
    }
}
