package com.wonnabe.goal.service;

import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.dto.GoalListResponseDTO;

import java.util.UUID;

public interface GoalService {
    public GoalListResponseDTO getGoalList(UUID userId);

    public GoalDetailResponseDTO getGoalDetail(UUID userId, Long goalId);
}
