package com.wonnabe.goal.controller;

import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.service.GoalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Api(tags = "목표 관리")
public class GoalController {
    private final GoalService service;

    @ApiOperation(value = "목표 리스트", notes = "목표 리스트 조회")
    @GetMapping("")
    public ResponseEntity<GoalListResponseDTO> getGoalList() {
        GoalListResponseDTO list = service.getGoalList(UUID.fromString("02747659-2dd1-41d6-80a7-131b9ddfac97")); // TODO: 실제 auth user
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "목표 상세", notes = "목표 세부사항, 추천 리스트 조회")
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponseDTO> getGoalDetail(
            @PathVariable Long goalId) {
        GoalDetailResponseDTO detail = service.getGoalDetail(UUID.fromString("02747659-2dd1-41d6-80a7-131b9ddfac97"), goalId); // TODO: 실제 auth user
        return ResponseEntity.ok(detail);
    }

    @ApiOperation(value = "목표 생성", notes = "새로운 목표 추가")
    @PostMapping("")
    public ResponseEntity<GoalCreateResponseDTO> createGoal(
            @RequestBody GoalCreateRequestDTO request
    ) {
        GoalCreateResponseDTO create = service.createGoal(UUID.fromString("02747659-2dd1-41d6-80a7-131b9ddfac97"), request); // TODO: 실제 auth user
        return ResponseEntity.ok(create);
    }

    @ApiOperation(value = "목표 상태 수정", notes = "목표 상태 수정")
    @PatchMapping("/{goalId}")
    public ResponseEntity<GoalSummaryResponseDTO> updateGoal(
            @PathVariable Long goalId,
            @RequestBody GoalStatusUpdateRequestDTO request
    ) {
        try {
            request.validate();
            String status = request.getStatus();
            GoalSummaryResponseDTO result;

            if ("PUBLISHED".equals(status)) {
                result = service.publishAsReport(UUID.fromString("02747659-2dd1-41d6-80a7-131b9ddfac97"), goalId, request.getSelectedProductId()); // TODO: 실제 auth user
            } else if ("ACHIEVED".equals(status)) {
                result = service.achieveGoal(UUID.fromString("02747659-2dd1-41d6-80a7-131b9ddfac97"), goalId);
            } else {
                return ResponseEntity.badRequest().body(new GoalSummaryResponseDTO());
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
