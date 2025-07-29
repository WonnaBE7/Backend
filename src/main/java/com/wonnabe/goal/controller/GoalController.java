package com.wonnabe.goal.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.service.GoalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public ResponseEntity<GoalListResponseDTO> getGoalList(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        String userId = customUser.getUser().getUserId();
        GoalListResponseDTO list = service.getGoalList(userId);
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "목표 상세", notes = "목표 세부사항, 추천 리스트 조회")
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponseDTO> getGoalDetail(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable Long goalId) {
        String userId = customUser.getUser().getUserId();
        GoalDetailResponseDTO detail = service.getGoalDetail(userId, goalId);
        return ResponseEntity.ok(detail);
    }

    @ApiOperation(value = "목표 생성", notes = "새로운 목표 추가")
    @PostMapping("")
    public ResponseEntity<GoalCreateResponseDTO> createGoal(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody GoalCreateRequestDTO request
    ) {
        String userId = customUser.getUser().getUserId();
        GoalCreateResponseDTO create = service.createGoal(userId, request);
        return ResponseEntity.ok(create);
    }

    @ApiOperation(value = "목표 상태 수정", notes = "목표 상태 수정")
    @PatchMapping("/{goalId}")
    public ResponseEntity<GoalSummaryResponseDTO> updateGoal(
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
            } else if ("ACHIEVED".equals(status)) {
                result = service.achieveGoal(userId, goalId);
            } else {
                return ResponseEntity.badRequest().body(new GoalSummaryResponseDTO());
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
