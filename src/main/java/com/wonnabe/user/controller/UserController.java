package com.wonnabe.user.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.DiagnosisHistoryResponse;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UpdateWonnabeRequest;
import com.wonnabe.user.dto.UserInfoResponse;
import com.wonnabe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * 로그인한 사용자의 워너비(선택한 금융 성향) 정보를 저장합니다.
     * - 사용자가 선택한 워너비 ID 배열을 User_Info 테이블의 selected_wonnabe_ids 필드에 저장합니다.
     * - JSON 형태로 저장되며, 기존 데이터를 덮어씁니다.
     *
     * @param user 인증된 사용자 정보 (SecurityContext에서 주입)
     * @param request UpdateWonnabeRequest 객체 (선택된 워너비 ID 배열 포함)
     * @return 성공 여부를 나타내는 JSON 응답, 상태코드 200 OK
     */
    @PatchMapping("/mypage/wonnabe")
    public ResponseEntity<Map<String, Boolean>> updateWonnabe(@AuthenticationPrincipal CustomUser user,
                                                              @RequestBody UpdateWonnabeRequest request) {
        userService.updateWonnabe(user, request);
        return ResponseEntity.ok(Map.of("isSuccess", true));
    }

    /**
     * 특정 사용자의 진단 결과 히스토리를 조회합니다.
     * - 과거 진단 결과 리스트를 조회하며, 월별 변화 추적용으로 사용됩니다.
     * - 진단 날짜를 기준으로 최신 순으로 정렬하여 반환합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return DiagnosisHistoryResponse 객체 (진단 히스토리 리스트 포함), 상태코드 200 OK
     */
    @GetMapping("/users/{id}/nowme/history")
    public ResponseEntity<DiagnosisHistoryResponse> getNowmeHistory(@PathVariable("id") String id) {
        return ResponseEntity.ok(userService.getNowmeHistory(id));
    }
}
