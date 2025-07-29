package com.wonnabe.goal.mapper;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.dto.GoalSummaryResponseDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GoalMapper {

    public List<GoalSummaryResponseDTO> getGoalList(@Param("userId") String userId);

    public GoalDetailResponseDTO getGoal(@Param("userId") String userId, @Param("goalId") Long goalId);

    public GoalSummaryResponseDTO getGoalSummaryById(@Param("goalId") Long goalId);

    public int createGoal(GoalVO goalVO);

    public int updateGoalStatusToPublished(@Param("goalId") Long goalId,
                                           @Param("selectedProductId") Long selectedProductId);

    public int updateGoalStatusToAchieved(@Param("goalId") Long goalId,
                                          @Param("achievedDate") LocalDateTime achievedDate);

    public Integer getNowmeIdByUserId(@Param("userId") String userId);

    public String getNowmeNameByNowmeId(@Param("nowmeId") Integer nowmeId);
}
