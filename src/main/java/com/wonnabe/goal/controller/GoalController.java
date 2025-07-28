package com.wonnabe.goal.controller;

import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.dto.GoalListResponseDTO;
import com.wonnabe.goal.service.GoalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
