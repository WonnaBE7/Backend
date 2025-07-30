package com.wonnabe.user.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UserInfoResponse;
import com.wonnabe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     * - AccessToken을 통해 인증된 사용자 정보를 기반으로
     *   유저의 이름, 이메일 등 정보를 조회합니다.
     *
     * @param user 인증된 사용자 정보 (SecurityContext에서 주입)
     * @return UserInfoResponse 객체 (사용자 정보 포함), 상태코드 200 OK
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(userService.getUserInfo(user));
    }

    /**
     * 로그인한 사용자의 이름 또는 비밀번호를 수정합니다.
     * - 인증된 사용자의 요청에 따라 이름 변경 또는 비밀번호 변경을 처리합니다.
     * - 비밀번호 변경 시, 현재 비밀번호와 새로운 비밀번호를 비교 및 인코딩 처리
     *
     * @param user 인증된 사용자 정보 (SecurityContext에서 주입)
     * @param request UpdateUserRequest 객체 (변경 요청 정보 포함)
     * @return 상태코드 200 OK (변경 성공)
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyInfo(@AuthenticationPrincipal CustomUser user,
                                             @RequestBody UpdateUserRequest request) {
        userService.updateUserInfo(user, request);
        return ResponseEntity.ok().build();
    }
}
