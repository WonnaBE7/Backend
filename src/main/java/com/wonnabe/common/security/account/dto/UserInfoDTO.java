package com.wonnabe.common.security.account.dto;

import com.wonnabe.common.security.account.domain.UserVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDTO {

    private String userId;
    private String name;
    private String email;

    public static UserInfoDTO of(UserVO user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
