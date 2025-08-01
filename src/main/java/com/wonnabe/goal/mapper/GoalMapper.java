package com.wonnabe.goal.mapper;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.GoalDetailResponseDTO;
import com.wonnabe.goal.dto.GoalSummaryResponseDTO;
import com.wonnabe.product.domain.SavingsProductVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface GoalMapper {

    public List<GoalSummaryResponseDTO> getGoalList(@Param("userId") String userId, @Param("status") String status);

    public GoalDetailResponseDTO getGoal(@Param("userId") String userId, @Param("goalId") Long goalId);

    public GoalSummaryResponseDTO getGoalSummaryById(@Param("goalId") Long goalId);

    public List<RecommendedProductVO> getRecommendedProductList(@Param("goalId") Long goalId);

    public int createGoal(GoalVO goalVO);

    public void insertRecommendedProductList(@Param("recommendations") List<RecommendedProductVO> recommendations);

    public int updateGoalStatusToPublished(@Param("goalId") Long goalId,
                                           @Param("selectedProductId") Long selectedProductId,
                                           @Param("saveAmount") BigDecimal saveAmount,
                                           @Param("expectedTotalAmount") BigDecimal expectedTotalAmount);

    public int updateGoalStatusToAchieved(@Param("goalId") Long goalId,
                                          @Param("achievedDate") LocalDateTime achievedDate);

    public RecommendedProductVO findRecommendedProductById(@Param("productId") Long productId,
                                                           @Param("goalId") Long goalId);

    public Integer getNowmeIdByUserId(@Param("userId") String userId);

    public String getNowmeNameByNowmeId(@Param("nowmeId") Integer nowmeId);

    public List<SavingsProductVO> getSavingsProductList();
}
