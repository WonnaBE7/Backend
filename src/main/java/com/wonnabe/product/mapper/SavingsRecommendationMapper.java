package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SavingsRecommendationMapper {
    // 모든 예적금 점수 조회
    List<SavingsProductVO> getAllSavingsScores();

    // 사용자의 소득/고용 정보 조회
    UserInfoVO getUserIncomeInfo(String userId);
}