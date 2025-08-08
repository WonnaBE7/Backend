package com.wonnabe.user.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.user.dto.*;
import com.wonnabe.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Log4j2
public class UserController {

    private final UserService userService;

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUser user) {
        try {
            UserInfoResponse result = userService.getUserInfo(user);
            return JsonResponse.ok("사용자 정보 조회 성공", result);
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보 조회 실패");
        }
    }

    /**
     * 로그인한 사용자의 이름 또는 비밀번호를 수정합니다.
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(@AuthenticationPrincipal CustomUser user,
                                          @RequestBody UpdateUserRequest request) {
        try {
            userService.updateUserInfo(user, request);
            return JsonResponse.ok("사용자 정보 수정 성공");
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보 수정 실패");
        }
    }

    /**
     * 로그인한 사용자의 워너비(선택한 금융 성향) 정보를 저장합니다.
     */
    @PatchMapping("/mypage/wonnabe")
    public ResponseEntity<?> updateWonnabe(@AuthenticationPrincipal CustomUser user,
                                           @RequestBody UpdateWonnabeRequest request) {
        try {
            userService.updateWonnabe(user, request);
            return JsonResponse.ok("워너비 정보 수정 성공", Map.of("isSuccess", true));
        } catch (Exception e) {
            log.error("워너비 정보 수정 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "워너비 정보 수정 실패");
        }
    }

    /**
     * 로그인한 사용자의 진단 결과 히스토리를 조회합니다. (실제 로그인 사용자만)
     */
    @GetMapping("/users/{id}/nowme/history")
    public ResponseEntity<?> getNowmeHistory(@PathVariable("id") String id,
                                             @AuthenticationPrincipal CustomUser user) {
        try {
            // 보안: 본인 정보만 조회 가능
            String currentUserId = user.getUser().getUserId();
            if (!currentUserId.equals(id)) {
                return JsonResponse.error(HttpStatus.FORBIDDEN, "본인의 히스토리만 조회할 수 있습니다.");
            }

            DiagnosisHistoryResponse result = userService.getNowmeHistory(id);
            return JsonResponse.ok("진단 히스토리 조회 성공", result);
        } catch (Exception e) {
            log.error("진단 히스토리 조회 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "진단 히스토리 조회 실패");
        }
    }

    /**
     * 로그인한 사용자의 상세 정보를 조회합니다.
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserDetail(@AuthenticationPrincipal CustomUser user) {
        try {
            String userId = user.getUser().getUserId();
            UserDetailResponse result = userService.getUserDetail(userId);

            if (result.getCode() == 404) {
                return JsonResponse.error(HttpStatus.NOT_FOUND, result.getMessage());
            }

            return JsonResponse.ok("사용자 상세 정보 조회 성공", result.getData());
        } catch (Exception e) {
            log.error("사용자 상세 정보 조회 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 상세 정보 조회 실패");
        }
    }

    /**
     * 로그인한 사용자의 상세 정보를 등록합니다.
     */
    @PostMapping("/info")
    public ResponseEntity<?> createUserDetail(@AuthenticationPrincipal CustomUser user,
                                              @RequestBody UserDetailRequest request) {
        try {
            String userId = user.getUser().getUserId();
            request = UserDetailRequest.builder()
                    .userId(userId)
                    .lifestyleSmoking(request.getLifestyleSmoking())
                    .lifestyleDrinking(request.getLifestyleDrinking())
                    .lifestyleExercise(request.getLifestyleExercise())
                    .householdSize(request.getHouseholdSize())
                    .lifestyleFamilyMedical(request.getLifestyleFamilyMedical())
                    .lifestyleBeforeDiseases(request.getLifestyleBeforeDiseases())
                    .incomeJobType(request.getIncomeJobType())
                    .incomeAnnualAmount(request.getIncomeAnnualAmount())  // 추가
                    .build();

            userService.createUserDetail(request);
            return JsonResponse.ok("사용자 정보가 성공적으로 생성되었습니다.",
                    Map.of("userId", userId, "createdAt", new Date()));
        } catch (RuntimeException e) {
            return JsonResponse.error(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("사용자 상세 정보 등록 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 상세 정보 등록 실패");
        }
    }

    /**
     * 로그인한 사용자의 상세 정보를 수정합니다.
     */
    @PutMapping("/info")
    public ResponseEntity<?> updateUserDetail(@AuthenticationPrincipal CustomUser user,
                                              @RequestBody UserDetailRequest request) {
        try {
            String userId = user.getUser().getUserId();
            request = UserDetailRequest.builder()
                    .userId(userId)
                    .lifestyleSmoking(request.getLifestyleSmoking())
                    .lifestyleDrinking(request.getLifestyleDrinking())
                    .lifestyleExercise(request.getLifestyleExercise())
                    .householdSize(request.getHouseholdSize())
                    .lifestyleFamilyMedical(request.getLifestyleFamilyMedical())
                    .lifestyleBeforeDiseases(request.getLifestyleBeforeDiseases())
                    .incomeJobType(request.getIncomeJobType())
                    .incomeAnnualAmount(request.getIncomeAnnualAmount())  // 추가
                    .build();

            List<String> updatedFields = userService.updateUserDetail(request);
            return JsonResponse.ok("사용자 정보가 성공적으로 수정되었습니다.",
                    Map.of("userId", userId, "updatedFields", updatedFields));
        } catch (RuntimeException e) {
            return JsonResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("사용자 상세 정보 수정 실패: {}", e.getMessage());
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 상세 정보 수정 실패");
        }
    }
}