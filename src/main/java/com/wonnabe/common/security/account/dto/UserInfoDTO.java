package com.wonnabe.common.security.account.dto;

import com.wonnabe.common.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    String username;
    String email;
    List<String> roles;

    public static UserInfoDTO of(MemberVO member) {
        return new UserInfoDTO(
                member.getUsername(),
                member.getEmail(),
                member.getAuthList().stream()
                        .map(a-> a.getAuth())
                        .toList()
        );
    }
}