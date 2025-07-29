package com.wonnabe.goal.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.service.GoalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
@Api(tags = "목표 관리")
public class GoalController {
    private final GoalService service;

    public GoalController(@Qualifier("goalServiceImpl") GoalService service) {
        this.service = service;
    }

    @ApiOperation(value = "목표 리스트", notes = "목표 리스트 조회")
    @GetMapping("")
    public ResponseEntity<Object> getGoalList(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        String userId = customUser.getUser().getUserId();
        GoalListResponseDTO list = service.getGoalList(userId);
        return JsonResponse.ok("목표 리스트 조회 성공", list);
    }

    @ApiOperation(value = "목표 상세", notes = "목표 세부사항, 추천 리스트 조회")
    @GetMapping("/{goalId}")
    public ResponseEntity<Object> getGoalDetail(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        String userId = customUser.getUser().getUserId();
        GoalDetailResponseDTO detail = service.getGoalDetail(userId, goalId);
        return JsonResponse.ok("목표 보고서 조회 성공", detail);
    }

    @ApiOperation(value = "목표 생성", notes = "새로운 목표 추가")
    @PostMapping("")
    public ResponseEntity<Object> createGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody GoalCreateRequestDTO request
    ) {
        String userId = customUser.getUser().getUserId();
        GoalCreateResponseDTO create = service.createGoal(userId, request);
        return JsonResponse.ok("새 목표 생성 성공", create);
    }

    @ApiOperation(value = "목표 상태 수정", notes = "목표 상태 수정")
    @PatchMapping("/{goalId}")
    public ResponseEntity<Object> updateGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId,
            @RequestBody GoalStatusUpdateRequestDTO request
    ) {
        String userId = customUser.getUser().getUserId();
        try {
            request.validate();
            String status = request.getStatus();
            GoalSummaryResponseDTO result;

            if ("PUBLISHED".equals(status)) {
                result = service.publishAsReport(userId, goalId, request.getSelectedProductId());
                return JsonResponse.ok("목표가 보고서로 성공적으로 저장되었습니다", result);
            } else if ("ACHIEVED".equals(status)) {
                result = service.achieveGoal(userId, goalId);
                return JsonResponse.ok("목표가 완료처리 되었습니다", result);
            } else {
                return JsonResponse.error(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 값입니다.");
            }
        } catch (IllegalArgumentException e) {
            return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}
