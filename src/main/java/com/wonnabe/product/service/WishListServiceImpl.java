package com.wonnabe.product.service;


import static com.wonnabe.product.dto.WishProductResponseDTO.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.BasicUserInfoDTO;
import com.wonnabe.product.dto.WishListRequestDTO;
import com.wonnabe.product.dto.WishProductResponseDTO;
import com.wonnabe.product.mapper.CardMapper;
import com.wonnabe.product.mapper.InsuranceMapper;
import com.wonnabe.product.mapper.SavingsMapper;
import com.wonnabe.product.mapper.WishListMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("wishListServiceImpl")
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

	// 페르소나 이름 매핑
	private static final Map<Integer, String> PERSONA_NAMES = Map.ofEntries(
			Map.entry(1, "자린고비형"),
			Map.entry(2, "소확행형"),
			Map.entry(3, "YOLO형"),
			Map.entry(4, "경험 소중형"),
			Map.entry(5, "새싹 투자형"),
			Map.entry(6, "공격 투자형"),
			Map.entry(7, "미래 준비형"),
			Map.entry(8, "가족 중심형"),
			Map.entry(9, "루틴러형"),
			Map.entry(10, "현상 유지형"),
			Map.entry(11, "균형 성장형"),
			Map.entry(12, "대문자P형")
	);

	// 페르소나별 정확한 가중치 [금리, 복리, 우대조건, 중도해지, 가입한도]
	private static final Map<Integer, double[]> SAVING_PERSONA_WEIGHTS = new HashMap<>() {{
		put(1, new double[]{0.3, 0.1, 0.2, 0.3, 0.2});    // 자린고비형
		put(2, new double[]{0.3, 0.15, 0.2, 0.2, 0.15});  // 소확행형
		put(3, new double[]{0.35, 0.1, 0.15, 0.3, 0.1});  // YOLO형
		put(4, new double[]{0.25, 0.15, 0.25, 0.2, 0.15}); // 경험 소중형
		put(5, new double[]{0.3, 0.2, 0.2, 0.2, 0.1});    // 새싹 투자형
		put(6, new double[]{0.4, 0.25, 0.15, 0.1, 0.1});  // 공격 투자형
		put(7, new double[]{0.25, 0.25, 0.3, 0.1, 0.1});  // 미래 준비형
		put(8, new double[]{0.2, 0.2, 0.2, 0.3, 0.1});    // 가족 중심형
		put(9, new double[]{0.25, 0.2, 0.2, 0.2, 0.15});  // 루틴러형
		put(10, new double[]{0.2, 0.15, 0.2, 0.3, 0.15}); // 현상 유지형
		put(11, new double[]{0.3, 0.2, 0.2, 0.2, 0.1});   // 균형 성장형
		put(12, new double[]{0.35, 0.1, 0.1, 0.35, 0.1}); // 대문자P형
	}};

	// 카드용 가중치
	private static final Map<Integer, double[]> CARD_PERSONA_WEIGHTS = new HashMap<>() {{
		put(1, new double[]{0.05, 0.10, 0.30, 0.05, 0.50});   // 자린고비형
		put(2, new double[]{0.20, 0.30, 0.20, 0.10, 0.20});   // 소확행형
		put(3, new double[]{0.30, 0.35, 0.05, 0.20, 0.10});   // YOLO형
		put(4, new double[]{0.25, 0.40, 0.10, 0.15, 0.10});   // 경험 소비형
		put(5, new double[]{0.05, 0.10, 0.40, 0.05, 0.40});   // 새싹 투자형
		put(6, new double[]{0.35, 0.30, 0.05, 0.20, 0.10});   // 공격 투자형
		put(7, new double[]{0.20, 0.30, 0.15, 0.20, 0.15});   // 미래 준비형
		put(8, new double[]{0.15, 0.30, 0.20, 0.10, 0.25});   // 가족 중심형
		put(9, new double[]{0.10, 0.30, 0.20, 0.25, 0.15});   // 루틴러형
		put(10, new double[]{0.05, 0.10, 0.30, 0.10, 0.45});  // 현상 유지형
		put(11, new double[]{0.20, 0.20, 0.20, 0.20, 0.20});  // 균형 성장형
		put(12, new double[]{0.25, 0.25, 0.05, 0.35, 0.10});  // 대문자P형
	}};

	// 보험용 가중치
	private static final Map<String, Map<String, Double>> PERSONA_WEIGHTS_INSURANCE = new HashMap<>() {{
		put("자린고비형", new HashMap<>() {{ put("가격_경쟁력", 0.45); put("보장한도", 0.15); put("보장범위", 0.15); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("소확행형", new HashMap<>() {{ put("가격_경쟁력", 0.35); put("보장한도", 0.20); put("보장범위", 0.20); put("자기부담금", 0.10); put("환급범위", 0.15); }});
		put("YOLO형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.20); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("경험 소중형", new HashMap<>() {{ put("가격_경쟁력", 0.25); put("보장한도", 0.25); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("새싹 투자형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.20); put("보장범위", 0.20); put("자기부담금", 0.20); put("환급범위", 0.10); }});
		put("공격 투자형", new HashMap<>() {{ put("가격_경쟁력", 0.40); put("보장한도", 0.20); put("보장범위", 0.15); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("미래 준비형", new HashMap<>() {{ put("가격_경쟁력", 0.20); put("보장한도", 0.30); put("보장범위", 0.30); put("자기부담금", 0.10); put("환급범위", 0.10); }});
		put("가족 중심형", new HashMap<>() {{ put("가격_경쟁력", 0.20); put("보장한도", 0.30); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("루틴러형", new HashMap<>() {{ put("가격_경쟁력", 0.25); put("보장한도", 0.25); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("현상 유지형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.25); put("보장범위", 0.20); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("균형 성장형", new HashMap<>() {{ put("가격_경쟁력", 0.25); put("보장한도", 0.25); put("보장범위", 0.25); put("자기부담금", 0.15); put("환급범위", 0.10); }});
		put("대문자P형", new HashMap<>() {{ put("가격_경쟁력", 0.30); put("보장한도", 0.20); put("보장범위", 0.20); put("자기부담금", 0.20); put("환급범위", 0.10); }});
	}};

	private final WishListMapper wishListMapper;

	private final CardMapper cardMapper;

	private final InsuranceMapper insuranceMapper;

	private final SavingsMapper savingsMapper;

	// 관심 상품 아이디 리스트로 변환
	public List<Long> getMatchedFilters(String wishList) {
		// 예: ["1", "2"] → List<Integer>로 변환
		String ids = wishList.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
		return Arrays.stream(ids.split(","))
				.filter(s -> !s.isEmpty())
				.map(Long::parseLong)
				.collect(Collectors.toList());
	}

	@Override
	public WishProductResponseDTO findWishProductByUserId(String userId) {
		// 선호 상품 아이디를 받음
		String wishList = wishListMapper.findWishListByUserId(userId);
		// 타입 변환
		List<Long> wishListId = getMatchedFilters(wishList);
		// 사용자 정보
		BasicUserInfoDTO basicUserInfo = cardMapper.findBasicUserInfoById(userId);

		// 사용자 정보가 없으면 예외 처리
		if (basicUserInfo == null) {
			throw new NoSuchElementException("사용자 정보가 부족하여 상품을 조회할 수 없습니다.");
		}

		// 예적금, 카드, 보험 상품 배열
		List<Savings> savings = new ArrayList<>();
		List<Card> cards = new ArrayList<>();
		List<Insurance> insurances = new ArrayList<>();

		// 내 관심 상품 조회
		for (Long id : wishListId) {
			// 예적금일 경우
			if (id < 2000 && id >= 1000) {

				double[] baseWeights = SAVING_PERSONA_WEIGHTS.get(basicUserInfo.getNowMeId()).clone();

				// 소득/고용상태로 가중치 조정
				double[] adjustedWeights = adjustWeightsByIncomeSaving(
						baseWeights,
						basicUserInfo.getIncomeSourceType(),
						basicUserInfo.getIncomeEmploymentStatus()
				);

				// 상품 정보
				SavingsProductVO savingProduct = savingsMapper.findById(id);

				// 점수 계산
				double totalScore = calculateScoreSaving(savingProduct, adjustedWeights);

				// 값을 배열에 저장
				Savings saving = Savings.builder()
						.productType("savings")
						.productId(Long.toString(savingProduct.getProductId()))
						.productName(savingProduct.getProductName())
						.bankName(savingProduct.getBankName())
						.baseRate(savingProduct.getBaseRate())
						.maxRate(Math.round(savingProduct.getMaxRate() * 100.0) / 100.0)
						.totalScore(Math.round(totalScore * 10.0) / 10.0)
						.build();

				savings.add(saving);
			} else if (id >= 2000 && id < 3000) {
				CardProductVO cardProduct = cardMapper.findById(id);

				double[] baseWeights = CARD_PERSONA_WEIGHTS.get(basicUserInfo.getNowMeId()).clone();
				double[] adjustedWeights =  adjustWeightsByIncomeCard(baseWeights,
						basicUserInfo.getIncomeAnnualAmount());

				int score = (int) calculateScoreCard(cardProduct, adjustedWeights, basicUserInfo.getPreviousConsumption());

				Card card = Card.builder()
						.productType("card")
						.cardId(Long.toString(cardProduct.getProductId()))
						.cardName(cardProduct.getCardName())
						.cardCompany(cardProduct.getCardCompany())
						.cardType(cardProduct.getCardTypeLabel())
						.matchScore(score)
						.mainBenefit(cardProduct.getBenefitLimit())
						.annualFeeDomestic(cardProduct.getAnnualFeeDomestic())
						.annualFeeOverseas(cardProduct.getAnnualFeeOverSeas())
						.build();

				cards.add(card);
			} else if (id >= 3000 && id < 4000) {
				// 기본 가중치 가져오기
				Map<String, Double> baseWeights = new HashMap<>(PERSONA_WEIGHTS_INSURANCE.
						get(PERSONA_NAMES.get(basicUserInfo.getNowMeId())));

				// 건강/생활습관으로 가중치 조정
				Map<String, Double> adjustedWeights = adjustWeightsByHealthAndLifestyle(
						baseWeights,
						basicUserInfo.getSmokingStatus(),
						basicUserInfo.getFamilyMedicalHistory(),
						basicUserInfo.getPastMedicalHistory(),
						basicUserInfo.getExerciseFrequency(),
						basicUserInfo.getDrinkingFrequency()
				);

				InsuranceProductVO insuranceProduct = insuranceMapper.findById(id);
				double totalScore = calculateInsuranceScore(insuranceProduct, adjustedWeights);

				Insurance insurance = Insurance.builder()
						.productType("insurance")
						.productId(Long.toString(insuranceProduct.getProductId()))
						.productName(insuranceProduct.getProductName())
						.providerName(insuranceProduct.getProviderName())
						.coverageType(insuranceProduct.getCoverageType())
						.coverageLimit(insuranceProduct.getCoverageLimit())
						.totalScore(Math.round(totalScore * 10.0) / 10.0)
						.build();

				insurances.add(insurance);
			}
		}
		int totalCount = insurances.size() + cards.size() + savings.size();

		return WishProductResponseDTO.builder()
				.totalCount(totalCount)
				.savings(savings)
				.cards(cards)
				.insurances(insurances)
				.build();
	}

	@Override
	public void updateWishList(String userId, WishListRequestDTO userRequest) {
		String action = userRequest.getAction();
		String productId = userRequest.getProductId();

		String wishList = wishListMapper.findWishListByUserId(userId);

		if (action.equals("add")) {
			if (wishList.contains("\"" + productId + "\"")) {
				throw new IllegalArgumentException("이미 존재하는 아이디를 입력하였습니다");
			}
			wishListMapper.addWishList(userId, productId);
		} else if (action.equals("remove")) {
			if (!wishList.contains("\"" + productId + "\"")) {
				throw new NoSuchElementException("해당 아이디는 이미 존재하지 않습니다.");
			}
			wishListMapper.deleteWishList(userId, productId);
		}
	}


	// 카드 활용도를 계산함
	public int calculatePerformanceRate(long performanceCondition, double monthlyUsage) {
		double performanceRate = monthlyUsage / performanceCondition * 100;
		if (performanceRate > 100) {
			performanceRate = 100;
		}
		return (int) performanceRate;
	}

	// 점수 계산
	public double calculateScoreCard(CardProductVO card, double[] weights, double amount) {
		List<Integer> score = card.getCardScores();
		int performanceRate = calculatePerformanceRate(card.getPerformanceCondition(), amount);
		int usageScore = calculateUsageScoreCard(performanceRate);
		score.set(3, usageScore);
		String updatedScore = score.toString();  // 예: [2, 3, 5, 4, 5]
		card.setCardScore(updatedScore);
		return (weights[0] * score.get(0) +
				weights[1] * score.get(1) +
				weights[2] * score.get(2) +
				weights[3] * score.get(3) +
				weights[4] * score.get(4)) * 20;
	}

	// 카드 활용 점수 계산
	public int calculateUsageScoreCard(int performanceRate) {
		if (performanceRate >= 100) return 5;
		if (performanceRate >= 80) return 4;
		if (performanceRate >= 60) return 3;
		if (performanceRate >= 40) return 2;
		return 1;
	}

	// 점수 계산
	private double calculateInsuranceScore(InsuranceProductVO product, Map<String, Double> weights) {
		double score = 0.0;
		score += weights.getOrDefault("가격_경쟁력", 0.0) * (product.getScorePriceCompetitiveness());
		score += weights.getOrDefault("보장한도", 0.0) * (product.getScoreCoverageLimit());
		score += weights.getOrDefault("보장범위", 0.0) * (product.getScoreCoverageScope());
		score += weights.getOrDefault("자기부담금", 0.0) * (product.getScoreDeductibleLevel());
		score += weights.getOrDefault("환급범위", 0.0) * (product.getScoreRefundScope());
		return score;
	}

	public double[] adjustWeightsByIncomeCard(double[] weights, double incomeAnnualAmount) {
		double[] adjusted = weights.clone();

		// 소득원별 조정
		if (incomeAnnualAmount >= 48000000.00) {
			adjusted[0] += 0.02;  // 확장성
			adjusted[1] += 0.02;  // 혜택 범위
			adjusted[2] -= 0.02; // 전월 실적
			adjusted[4] -= 0.02; // 연회비
		} else if (incomeAnnualAmount < 24000000.00) {
			adjusted[0] -= 0.02;  // 확장성
			adjusted[1] -= 0.02;  // 혜택 범위
			adjusted[2] += 0.02; // 전월 실적
			adjusted[4] += 0.02; // 연회비
		}

		// 정규화 (합 = 1)
		return normalizeWeights(adjusted);
	}

	// 건강/생활습관에 따른 가중치 조정
	private Map<String, Double> adjustWeightsByHealthAndLifestyle(
			Map<String, Double> weights,
			String smokingStatus,
			String familyMedicalHistory,
			String pastMedicalHistory,
			String exerciseFrequency,
			String drinkingFrequency) {

		Map<String, Double> adjusted = new HashMap<>(weights);

		// (‼️ 수정)
		// 흡연 여부
		if ("1".equalsIgnoreCase(String.valueOf(smokingStatus))) {
			adjusted.compute("보장범위", (k, v) -> v != null ? v + 0.05 : 0.05);        // 질병 리스크 확대
			adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 보험사 리스크 반영
		} else {
			adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 비흡연자 우대
			adjusted.compute("환급범위", (k, v) -> v != null ? v + 0.03 : 0.03);        // 장기 계약 유도
		}


		// 가족 병력
		if ("1".equalsIgnoreCase(String.valueOf(familyMedicalHistory))) {
			adjusted.compute("보장한도", (k, v) -> v != null ? v + 0.05 : 0.05);    // 만성질환 대비
			adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 보험사 부담 반영
		}



		// 과거 병력
		if ("1".equalsIgnoreCase(String.valueOf(pastMedicalHistory))) {
			adjusted.compute("보장범위", (k, v) -> v != null ? v + 0.05 : 0.05);        // 재발 가능성 고려
			adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.05 : 0.05);  // 보험사 리스크 반영
		} else {
			adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 무병력 우대
		}


		// 운동 빈도
		if ("1".equalsIgnoreCase(String.valueOf(exerciseFrequency))) {
			adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 건강 습관 우대
			adjusted.compute("환급범위", (k, v) -> v != null ? v + 0.03 : 0.03);        // 장기계약 유지 유도
		} else if ("0".equalsIgnoreCase(String.valueOf(exerciseFrequency))) {
			adjusted.compute("보장한도", (k, v) -> v != null ? v + 0.05 : 0.05);        // 건강 리스크 고려
			adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 보험사 리스크 반영
		}


		// 음주 빈도
		if ("1".equalsIgnoreCase(String.valueOf(drinkingFrequency))) {
			adjusted.compute("보장범위", (k, v) -> v != null ? v + 0.05 : 0.05);        // 간질환 등 대비
			adjusted.compute("자기부담금수준", (k, v) -> v != null ? v + 0.03 : 0.03);  // 고위험 반영
		} else if ("0".equalsIgnoreCase(String.valueOf(drinkingFrequency))) {
			adjusted.compute("가격_경쟁력", (k, v) -> v != null ? v + 0.05 : 0.05);     // 건강군 우대
			adjusted.compute("환급범위", (k, v) -> v != null ? v + 0.03 : 0.03);
		}

		// 정규화 (합 = 1)
		return normalizeWeightsInsurance(adjusted);
	}
	// 가중치 정규화
	private Map<String, Double> normalizeWeightsInsurance(Map<String, Double> weights) {
		double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
		if (sum == 0) {
			return weights;
		}
		return weights.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / sum));
	}


	// 소득/고용상태에 따른 가중치 조정
	private double[] adjustWeightsByIncomeSaving(double[] weights, String incomeSource, String employment) {
		double[] adjusted = weights.clone();

		// (‼️ 수정)
		// 소득원별 조정
		if (incomeSource != null && !incomeSource.isEmpty()) {
			switch (incomeSource) {
				case "근로소득":
					adjusted[0] += 0.05;  // 금리
					adjusted[1] += 0.03;  // 단복리
					break;
				case "사업소득":
					adjusted[3] += 0.05;  // 중도해지
					adjusted[2] += 0.03;  // 우대조건
					break;
				case "기타소득":
					adjusted[4] += 0.05;  // 최대한도
					adjusted[3] += 0.03;  // 중도해지
					break;
			}
		}

		// 고용상태별 조정
		if (employment != null && !employment.isEmpty()) {
			switch (employment) {
				case "정규직":
					adjusted[0] += 0.05;  // 금리
					adjusted[1] += 0.03;  // 단복리
					break;
				case "학생":
					adjusted[2] += 0.05;  // 우대조건
					adjusted[4] += 0.03;  // 최대한도
					break;
				case "무직":
					adjusted[3] += 0.05;  // 중도해지
					adjusted[1] += 0.03;  // 단복리
					break;
			}
		}

		// 정규화 (합 = 1)
		return normalizeWeights(adjusted);
	}

	// 점수 계산
	private double calculateScoreSaving(SavingsProductVO score, double[] weights) {
		return (weights[0] * score.getScoreInterestRate() +
				weights[1] * score.getScoreInterestType() +
				weights[2] * score.getScorePreferentialCondition() +
				weights[3] * score.getScoreCancelBenefit() +
				weights[4] * score.getScoreMaxAmount());
	}

	// 가중치 정규화
	public double[] normalizeWeights(double[] weights) {
		double sum = Arrays.stream(weights).sum();
		if (sum == 0) {
			return weights;
		}
		return Arrays.stream(weights).map(w -> w / sum).toArray();
	}
}