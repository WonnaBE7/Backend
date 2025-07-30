package com.wonnabe.user.service;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UserInfoResponse;
import com.wonnabe.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     * - SecurityContext에 저장된 사용자 정보를 기반으로
     *   사용자 ID, 이름, 이메일 정보를 추출하여 반환합니다.
     *
     * @param user SecurityContext에서 주입된 인증 사용자(CustomUser)
     * @return UserInfoResponse 객체 (userId, name, email 포함)
     */
    public UserInfoResponse getUserInfo(CustomUser user) {
        return UserInfoResponse.builder()
                .userId(user.getUser().getUserId())
                .name(user.getUser().getName())
                .email(user.getUser().getEmail())
                .build();
    }

    /**
     * 로그인한 사용자의 이름 또는 비밀번호를 수정합니다.
     * - 요청 객체의 이름 및 비밀번호 정보를 기반으로 DB에 반영
     * - 비밀번호는 암호화하여 저장합니다.
     *
     * @param user SecurityContext에서 주입된 인증 사용자(CustomUser)
     * @param request UpdateUserRequest 객체 (수정할 이름, 비밀번호 포함)
     */
    public void updateUserInfo(CustomUser user, UpdateUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        userMapper.updateUser(user.getUser().getUserId(), request.getName(), encodedPassword);
    }
}
