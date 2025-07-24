package com.wonnabe.goal.controller;

import com.wonnabe.goal.service.GoalService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Api(tags = "목표 관리")
public class GoalController {
    private final GoalService service;

}
