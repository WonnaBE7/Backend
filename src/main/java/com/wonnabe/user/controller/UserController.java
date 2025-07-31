package com.wonnabe.user.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UserInfoResponse;
import com.wonnabe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Object> getMyInfo(@AuthenticationPrincipal CustomUser user) {
        try {
            if (user == null || user.getUser() == null) {
                return JsonResponse.error(HttpStatus.UNAUTHORIZED, "로그인 정보가 확인되지 않습니다. 다시 로그인해 주세요.");
            }
            return JsonResponse.ok("사용자 정보 조회 성공", userService.getUserInfo(user));
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보를 불러오는 데 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
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
    public ResponseEntity<Object> updateMyInfo(@AuthenticationPrincipal CustomUser user,
                                               @RequestBody UpdateUserRequest request) {
        try {
            userService.updateUserInfo(user, request);
            return JsonResponse.ok("사용자 정보가 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
