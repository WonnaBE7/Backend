package com.wonnabe.goal.service;

import com.wonnabe.goal.domain.GoalVO;
import com.wonnabe.goal.domain.RecommendedProductVO;
import com.wonnabe.goal.dto.*;
import com.wonnabe.goal.mapper.GoalMapper;
import com.wonnabe.product.domain.SavingsProductVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("goalServiceImpl")
@RequiredArgsConstructor
@Log4j2
public class GoalServiceImpl implements GoalService {

    private final GoalMapper goalMapper;

    private final OpenAiService openAiService;

    // 상품 ID 범위 상수
    private static final long SAVINGS_PRODUCT_ID_START = 1000L;
    private static final long SAVINGS_PRODUCT_ID_END = 1500L;
    private static final long DEPOSIT_PRODUCT_ID_START = 1500L;
    private static final long DEPOSIT_PRODUCT_ID_END = 2000L;

    @Override
    public GoalListResponseDTO getGoalList(String userId, String status) {
        if (!status.equals("PUBLISHED") && !status.equals("ACHIEVED")) {
            throw new IllegalArgumentException("유효하지 않은 상태 값입니다. (허용: PUBLISHED, ACHIEVED)");
        }

        List<GoalSummaryResponseDTO> goalSummaries = goalMapper.getGoalList(userId, status);

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
    public GoalDetailResponseDTO getGoalDetail(String userId, Long goalId) {
        GoalDetailResponseDTO goalDetail = goalMapper.getGoal(userId, goalId);

        // 목표 존재하지 않는 예외
        if (goalDetail == null) {
            throw new NoSuchElementException("요청하신 목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }

        List<RecommendedProductDTO> recommendedProducts = RecommendedProductDTO.ofList(goalMapper.getRecommendedProductList(goalId));
        goalDetail.setRecommendedProducts(recommendedProducts);

        return goalDetail;
    }

    @Override
    @Transactional
    @SneakyThrows
    public GoalCreateResponseDTO createGoal(String userId, GoalCreateRequestDTO request) {
        // nowmeId 조회
        Integer nowmeId = goalMapper.getNowmeIdByUserId(userId);
        String nowmeName = null;
        String nowmeDescription = null;
        if (nowmeId != null) {
            nowmeName = goalMapper.getNowmeNameByNowmeId(nowmeId);
            nowmeDescription = goalMapper.getNowmeDescriptionByNowmeId(nowmeId);
        }

        // category 유효성 조회
        String categoryName = goalMapper.getCategoryNameById(request.getCategoryId());
        if (categoryName == null) {
            throw new IllegalArgumentException("해당하는 카테고리가 존재하지 않습니다.");
        }

        // 미래의 나에게 보내는 메시지 생성
        String futureMeMessage = generateFutureMeMessage(nowmeName, nowmeDescription, request.getGoalName(), request.getTargetAmount());

        GoalVO goalToInsert = GoalVO.builder()
                .userId(userId)
                .nowmeId(nowmeId)
                .categoryId(request.getCategoryId())
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .goalDurationMonths(request.getGoalDurationMonths())
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().plusMonths(request.getGoalDurationMonths()))
                .currentAmount(BigDecimal.valueOf(0))
                .progressRate(BigDecimal.valueOf(0))
                .isAchieved(false)
                .resultSummary(futureMeMessage)
                .status("DRAFT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mapper를 통해 DB 저장
        goalMapper.createGoal(goalToInsert);
        Long createdGoalId = goalToInsert.getId();

        List<RecommendedProductVO> recommendations = calculateRecommendedProductList(request, createdGoalId);

        if (!recommendations.isEmpty()) {
            goalMapper.insertRecommendedProductList(recommendations);
        }

        List<RecommendedProductDTO> recommendedProductList = RecommendedProductDTO.ofList(goalMapper.getRecommendedProductList(createdGoalId));

        // 반환 DTO 생성
        return GoalCreateResponseDTO.builder()
                .goalId(createdGoalId)
                .futureMeMessage(futureMeMessage)
                .recommendedProducts(recommendedProductList)
                .build();
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO publishAsReport(String userId, Long goalId, Long selectedProductId) {
        // 목표 존재 확인
        GoalDetailResponseDTO existingGoal = goalMapper.getGoal(userId, goalId);
        if (existingGoal == null) {
            throw new NoSuchElementException("목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }

        // 상품 존재 확인
        RecommendedProductVO selectedProduct = goalMapper.findRecommendedProductById(selectedProductId, goalId);
        if (selectedProduct == null) {
            throw new NoSuchElementException("해당하는 상품이 없습니다.");
        }

        goalMapper.updateGoalStatusToPublished(goalId, selectedProductId, selectedProduct.getSaveAmount(), selectedProduct.getExpectedTotalAmount());

        return goalMapper.getGoalSummaryById(goalId);
    }

    @Override
    @Transactional
    public GoalSummaryResponseDTO achieveGoal(String userId, Long goalId) {
        // 목표 존재 확인
        GoalDetailResponseDTO existingGoal = goalMapper.getGoal(userId, goalId);
        if (existingGoal == null) {
            throw new NoSuchElementException("목표 (ID: " + goalId + ")를 찾을 수 없습니다.");
        }

        goalMapper.updateGoalStatusToAchieved(goalId, LocalDateTime.now());

        return goalMapper.getGoalSummaryById(goalId);
    }

    private String generateFutureMeMessage(String nowmeName, String nowmeDescription, String goalName, BigDecimal targetAmount) {
        String name = (nowmeName != null) ? nowmeName : "나";
        String description = (nowmeDescription != null) ? nowmeDescription : "";

        // 1. GPT에 전달할 프롬프트(지시어)
        String prompt = String.format(
                "역할: **'%s'** 목표를 통해 **%s원**을 달성한 미래의 **%s**\n" +
                        "현재 금융 성향: %s\n\n" +
                        "임무: 과거의 나에게 보내는 300자 이내 메시지 작성\n\n" +
                        "반드시 포함할 내용:\n" +
                        "- '안녕, 과거의 나야'로 시작\n" +
                        "- '%s' 목표 달성 언급\n" +
                        "- '%s원' 금액 언급 (3자리마다 콤마 포함)\n" +
                        "- 달성 후 변화된 삶\n" +
                        "- 현재의 나에게 주는 격려\n\n" +
                        "작성 스타일: 따뜻하고 구체적이며 개인적인 톤으로, 입력받은 정보를 정확히 활용하여 작성",
                goalName, targetAmount.toPlainString(), name,
                description, goalName, targetAmount.toPlainString()
        );
        log.info("prompt: {}", prompt);

        // 2. OpenAI 서비스 호출
        try {
            String gptMessage = openAiService.getGptResponse(prompt);

            if (gptMessage != null && !gptMessage.isEmpty()) {
                return gptMessage;
            } else {
                return "목표를 꼭 달성하고 미래의 나에게 칭찬을 아끼지 마세요!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "목표를 꼭 이룰 수 있을 거에요!";
        }
    }

    private List<RecommendedProductVO> calculateRecommendedProductList(
            GoalCreateRequestDTO request, Long goalId) {

        BigDecimal targetAmount = request.getTargetAmount();
        int goalDurationMonths = request.getGoalDurationMonths();

        // 전체 상품 조회
        List<SavingsProductVO> allProducts = goalMapper.getSavingsProductList();
        List<RecommendedProductVO> recommendations = new ArrayList<>();

        for (SavingsProductVO product : allProducts) {
            // 최소 기간 필터링
            if (goalDurationMonths < product.getMinJoinPeriod()) {
                continue;
            }

            int actualJoinPeriod = Math.min(goalDurationMonths, product.getMaxJoinPeriod());

            BigDecimal maxRate = BigDecimal.valueOf(product.getMaxRate());
            BigDecimal annualRate = maxRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

            // 상품 타입에 따른 로직 분기
            if (product.getProductId() >= SAVINGS_PRODUCT_ID_START && product.getProductId() < SAVINGS_PRODUCT_ID_END) { // 적금
                BigDecimal monthlyDepositAmount; // 월 필요 납입액
                if ("단리".equals(product.getRateType())) {
                    monthlyDepositAmount = calculateMonthlyDepositSimple(targetAmount, actualJoinPeriod, annualRate);
                } else {
                    monthlyDepositAmount = calculateMonthlyDepositCompound(targetAmount, actualJoinPeriod, annualRate);
                }

                if (monthlyDepositAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 적금의 월 납입 한도(min/max) 체크
                if ((product.getMaxAmount() != null && monthlyDepositAmount.compareTo(BigDecimal.valueOf(product.getMaxAmount())) > 0) ||
                        (product.getMinAmount() != null && monthlyDepositAmount.compareTo(BigDecimal.valueOf(product.getMinAmount())) < 0)) {
                    continue;
                }

                BigDecimal expectedTotalAmount = calculateFinalAmountForSaving(monthlyDepositAmount, actualJoinPeriod, annualRate, product.getRateType());
                BigDecimal achievementRate = calculateAchievementRate(expectedTotalAmount, targetAmount);

                recommendations.add(RecommendedProductVO.builder()
                        .goalId(goalId)
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .bankName(product.getBankName())
                        .interestRate(maxRate)
                        .achievementRate(achievementRate)
                        .saveAmount(monthlyDepositAmount)
                        .expectedAchievementDate(LocalDate.now().plusMonths(actualJoinPeriod))
                        .expectedTotalAmount(expectedTotalAmount)
                        .build());
            } else if (product.getProductId() >= DEPOSIT_PRODUCT_ID_START && product.getProductId() < DEPOSIT_PRODUCT_ID_END) { // 예금
                BigDecimal initialPrincipal; // 최초 필요 원금
                if ("단리".equals(product.getRateType())) {
                    initialPrincipal = calculatePrincipalSimple(targetAmount, actualJoinPeriod, annualRate);
                } else {
                    initialPrincipal = calculatePrincipalCompound(targetAmount, actualJoinPeriod, annualRate);
                }

                if (initialPrincipal.compareTo(BigDecimal.ZERO) <= 0) continue;

                // 예금의 가입 금액 한도(min/max) 체크
                if ((product.getMaxAmount() != null && initialPrincipal.compareTo(BigDecimal.valueOf(product.getMaxAmount())) > 0) ||
                        (product.getMinAmount() != null && initialPrincipal.compareTo(BigDecimal.valueOf(product.getMinAmount())) < 0)) {
                    continue;
                }

                BigDecimal expectedTotalAmount = calculateFinalAmountForDeposit(initialPrincipal, actualJoinPeriod, annualRate, product.getRateType());
                BigDecimal achievementRate = calculateAchievementRate(expectedTotalAmount, targetAmount);

                recommendations.add(RecommendedProductVO.builder()
                        .goalId(goalId)
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .bankName(product.getBankName())
                        .interestRate(maxRate)
                        .achievementRate(achievementRate)
                        .saveAmount(initialPrincipal)
                        .expectedAchievementDate(LocalDate.now().plusMonths(actualJoinPeriod))
                        .expectedTotalAmount(expectedTotalAmount)
                        .build());
            }
        }

        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedProductVO::getInterestRate).reversed()  // 1. 이자 높은 순
                        .thenComparing(RecommendedProductVO::getSaveAmount))                    // 2. 납입액 적은 순
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * [적금/단리] 월 납입액 계산
     */
    private BigDecimal calculateMonthlyDepositSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        BigDecimal t = new BigDecimal(periodMonths);
        BigDecimal r_12 = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal interestPart = r_12.multiply(t.multiply(t.add(BigDecimal.ONE)).divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP));
        BigDecimal denominator = t.add(interestPart);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return targetAmount.divide(denominator, 0, RoundingMode.CEILING);
    }

    /**
     * [적금/복리] 월 납입액 계산
     */
    private BigDecimal calculateMonthlyDepositCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        BigDecimal i = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        if (i.compareTo(BigDecimal.ZERO) == 0) {
            return targetAmount.divide(new BigDecimal(periodMonths), 0, RoundingMode.CEILING);
        }
        BigDecimal numerator = targetAmount.multiply(i);
        BigDecimal denominator = (BigDecimal.ONE.add(i)).pow(periodMonths).subtract(BigDecimal.ONE);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return numerator.divide(denominator, 0, RoundingMode.CEILING);
    }

    /**
     * [예금/단리] 목표금액을 위한 필요 원금 계산
     */
    private BigDecimal calculatePrincipalSimple(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        // 기간을 연 단위로 변환
        BigDecimal n_years = new BigDecimal(periodMonths).divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        // 분모: 1 + r * n
        BigDecimal denominator = BigDecimal.ONE.add(annualRate.multiply(n_years));

        if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return targetAmount.divide(denominator, 0, RoundingMode.CEILING);
    }

    /**
     * [예금/복리] 목표금액을 위한 필요 원금 계산 (연 복리)
     */
    private BigDecimal calculatePrincipalCompound(BigDecimal targetAmount, int periodMonths, BigDecimal annualRate) {
        double n_years = (double) periodMonths / 12.0;

        // 분모: (1+r)^n, n이 정수가 아닐 수 있으므로 Math.pow 사용
        BigDecimal denominator = BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(annualRate).doubleValue(), n_years));

        if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return targetAmount.divide(denominator, 0, RoundingMode.CEILING);
    }

    /**
     * [적금] 최종 만기액 계산
     */
    private BigDecimal calculateFinalAmountForSaving(BigDecimal monthlyDeposit, int months, BigDecimal rate, String rateType) {
        // 월 납입금과 기간, 이율로 만기액을 순방향으로 계산하여 검증
        if ("단리".equals(rateType)) {
            BigDecimal t = new BigDecimal(months);
            BigDecimal r12 = rate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            BigDecimal principal = monthlyDeposit.multiply(t);
            BigDecimal interest = monthlyDeposit.multiply(r12).multiply(t.multiply(t.add(BigDecimal.ONE)).divide(new BigDecimal("2")));
            return principal.add(interest);
        } else {
            BigDecimal i = rate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            if (i.compareTo(BigDecimal.ZERO) == 0) return monthlyDeposit.multiply(new BigDecimal(months));
            BigDecimal factor = (BigDecimal.ONE.add(i)).pow(months).subtract(BigDecimal.ONE).divide(i, 10, RoundingMode.HALF_UP);
            return monthlyDeposit.multiply(factor);
        }
    }

    /**
     * [예금] 최종 만기액 계산
     */
    private BigDecimal calculateFinalAmountForDeposit(BigDecimal principal, int months, BigDecimal rate, String rateType) {
        // 예치 원금과 기간, 이율로 만기액을 순방향으로 계산하여 검증
        double n = (double) months / 12.0;
        if ("단리".equals(rateType)) {
            return principal.multiply(BigDecimal.ONE.add(rate.multiply(new BigDecimal(n))));
        } else {
            return principal.multiply(BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(rate).doubleValue(), n)));
        }
    }

    /**
     * 목표 달성률 계산 (예상 수령액 기준)
     */
    private BigDecimal calculateAchievementRate(BigDecimal expectedAmount, BigDecimal targetAmount) {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal rate = expectedAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        // 100%를 초과하면 100%로 제한
        return rate.min(BigDecimal.valueOf(100));
    }
}
