package com.wonnabe.goal.mapper;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.dto.GoalSummaryResponseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface GoalMapper {

    public List<GoalSummaryResponseDTO> getGoalList(@Param("userId") String userId);

    public GoalDetailResponseDTO getGoal(@Param("userId") String userId, @Param("goalId") Long goalId);
}
