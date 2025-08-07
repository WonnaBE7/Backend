package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InsuranceRecommendationMapper {

    // 사용자의 건강/생활습관 정보 조회
    UserIncomeInfoVO getUserHealthInfo(String userId);

    // 모든 보험 상품과 점수 조회
    List<InsuranceProductVO> getAllInsuranceScores();
}
