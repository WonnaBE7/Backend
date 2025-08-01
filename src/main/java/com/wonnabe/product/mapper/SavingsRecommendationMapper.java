package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SavingsRecommendationMapper {

    // 사용자의 소득/고용 정보 조회
    UserIncomeInfoVO getUserIncomeInfo(String userId);

    // 모든 적금 상품과 점수 조회
    List<SavingsProductVO> getAllSavingsScores();
}
