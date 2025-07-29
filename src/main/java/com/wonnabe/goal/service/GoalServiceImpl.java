package com.wonnabe.goal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.mapper.GoalMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalMapper goalMapper;
    private final ObjectMapper objectMapper;

    @Override
    public GoalListResponseDTO getGoalList(UUID userId) {
        List<GoalSummaryResponseDTO> goalSummaries = goalMapper.getGoalList(userId.toString());

        BigDecimal totalTargetAmount = goalSummaries.stream()
                .map(goal -> goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return GoalListResponseDTO.builder()
                .totalGoalCount(goalSummaries.size())
                .totalTargetAmount(totalTargetAmount)
                .goals(goalSummaries)
                .build();
    }

    @Override
    public GoalDetailResponseDTO getGoalDetail(UUID userId, Long goalId) {
        GoalDetailResponseDTO goalDetail = goalMapper.getGoal(userId.toString(), goalId);

        List<RecommendedProductDTO> recommendedProducts = new ArrayList<>(); // TODO: 추천된 리스트 가져오는 함수 필요
        goalDetail.setRecommendedProducts(recommendedProducts);

        return goalDetail;
    }

    @Override
    @Transactional
    @SneakyThrows
    public GoalCreateResponseDTO createGoal(UUID userId, GoalCreateRequestDTO request) {
        // nowmeId 조회
        Integer nowmeId = goalMapper.getNowmeIdByUserId(userId.toString());
        String nowmeName = null;
        if (nowmeId != null) {
            nowmeName = goalMapper.getNowmeNameByNowmeId(nowmeId);
        }

        // 월 저축액 계산
        // TODO: 예금/적금인지에 따라 계산 로직 달라지게 하기
        BigDecimal monthlySaveAmount = request.getTargetAmount()
                .divide(new BigDecimal(request.getGoalDurationMonths()), 0, RoundingMode.UP);

        // 추천 상품 목록 조회 및 이자 계산 로직
        // TODO: 실제 가져오기
        List<RecommendedProductDTO> recommendedProductList = Arrays.asList(
                RecommendedProductDTO.builder()
                        .id(1001L)
                        .name("하나 예금 상품")
                        .bank("하나은행")
                        .tags(Arrays.asList("단기", "고금리"))
                        .interestRate(3.5f)
                        .achievementRate(80)
                        .monthlyDepositAmount(monthlySaveAmount)
                        .expectedAchievementDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()).toString())
                        .expectedTotalAmount(monthlySaveAmount.multiply(new BigDecimal(request.getGoalDurationMonths())))
                        .build(),

                RecommendedProductDTO.builder()
                        .id(1010L)
                        .name("카카오 적금 상품")
                        .bank("카카오뱅크")
                        .tags(Arrays.asList("적금", "자유입출금"))
                        .interestRate(3.0f)
                        .achievementRate(70)
                        .monthlyDepositAmount(monthlySaveAmount)
                        .expectedAchievementDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()).toString())
                        .expectedTotalAmount(monthlySaveAmount.multiply(new BigDecimal(request.getGoalDurationMonths())))
                        .build()
        );

        // 예상 이자 및 총 수령액 계산
        // TODO: 실제 계산식 필요
        BigDecimal expectedTotalAmount = monthlySaveAmount.multiply(new BigDecimal(request.getGoalDurationMonths()));
        BigDecimal interestGain = expectedTotalAmount.multiply(BigDecimal.valueOf(0.03)).setScale(0, RoundingMode.DOWN); // 단순 3% 수익률

        // 미래의 나에게 보내는 메시지 생성
        String futureMeMessage = generateFutureMeMessage(nowmeName);
        
        // 추천 상품 ID 목록을 List<Long>으로 추출
        // TODO: 실제 예적금 추천
        String recommendedProductsStr = convertProductIdsToJsonArray(recommendedProductList); // 예: "1001,1002"

        GoalVO goalToInsert = GoalVO.builder()
                .userId(userId.toString())
                .nowmeId(nowmeId)
                .categoryId(request.getCategoryId())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .goalDurationMonths(request.getGoalDurationMonths())
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()))
                .monthlySaveAmount(monthlySaveAmount)
                .expectedTotalAmount(expectedTotalAmount)
                .interestGain(interestGain)
                .progressRate(0f)
                .isAchieved(false)
                .resultSummary(futureMeMessage)
                .recommendedProducts(recommendedProductsStr)
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mapper를 통해 DB 저장
        goalMapper.createGoal(goalToInsert);

        // 생성된 goalId 조회
        Long createdGoalId = goalToInsert.getId();

        // 반환 DTO 생성
        return GoalCreateResponseDTO.builder()
                .goalId(createdGoalId)
                .futureMeMessage(futureMeMessage)
                .recommendedProducts(recommendedProductList)
                .build();
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO publishAsReport(UUID userId, Long goalId, Long selectedProductId) {
        goalMapper.updateGoalStatusToPublished(goalId, selectedProductId);

        return goalMapper.getGoalSummaryById(goalId);
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO achieveGoal(UUID userId, Long goalId) {
        goalMapper.updateGoalStatusToAchieved(goalId, LocalDateTime.now());

        return goalMapper.getGoalSummaryById(goalId);
    }

    private String generateFutureMeMessage(String nowmeName) {
        // TODO: GPT API
        if (nowmeName == null) return "지금의 당신도 멋져요!";
        return nowmeName + "님, 목표를 꼭 달성하고 미래의 나에게 칭찬을 아끼지 마세요!";
    }

    private String convertProductIdsToJsonArray(List<RecommendedProductDTO> products) {
        List<Long> idList = products.stream()
                .map(RecommendedProductDTO::getId)
                .collect(Collectors.toList());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(idList); // 예: "[1,2,3]"
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
