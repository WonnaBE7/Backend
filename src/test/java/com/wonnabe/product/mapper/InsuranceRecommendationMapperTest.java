package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "springfox.documentation.enabled=false"  // 테스트에서 Swagger 비활성화
})

class InsuranceRecommendationMapperTest {

    @Autowired
    private InsuranceRecommendationMapper insuranceRecommendationMapper;

    private final String userId = "a1b2c3d4-e5f6-7890-ab12-cd34ef56gh78"; // 테스트용 사용자 ID

    @Test
    @DisplayName("[성공] 사용자 ID로 건강 및 페르소나 정보를 DB에서 조회")
    void getUserHealthInfo_성공() {
        // when
        UserIncomeInfoVO result = insuranceRecommendationMapper.getUserHealthInfo(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getPersonaIds());
        assertFalse(result.getPersonaIds().isEmpty());
        System.out.println("\n--- [InsuranceRecommendationMapperTest] 사용자 건강 및 페르소나 정보 조회 결과 ---");
        System.out.println("조회된 UserIncomeInfoVO 객체:");
        System.out.println("  - 사용자 ID: " + result.getUserId());
        System.out.println("  - 선택된 워너비 ID 문자열: " + result.getSelectedWonnabeIds());
        System.out.println("  - 파싱된 페르소나 ID 목록: " + result.getPersonaIds());
        System.out.println("  - 흡연 여부: " + result.getSmokingStatus());
        System.out.println("  - 가족 병력: " + result.getFamilyMedicalHistory());
        System.out.println("  - 과거 병력: " + result.getPastMedicalHistory());
        System.out.println("  - 운동 빈도: " + result.getExerciseFrequency());
        System.out.println("  - 음주 빈도: " + result.getDrinkingFrequency());
        System.out.println("-------------------------------------------------------------------");
    }

    @Test
    @DisplayName("[성공] 모든 보험 상품과 점수 정보를 DB에서 조회")
    void getAllInsuranceScores_성공() {
        // when
        List<InsuranceProductVO> results = insuranceRecommendationMapper.getAllInsuranceScores();

        // then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertNotNull(results.get(0).getPriceCompetitivenessScore());
        System.out.println("\n--- [InsuranceRecommendationMapperTest] 모든 보험 상품 및 점수 조회 결과 ---");
        System.out.println("조회된 InsuranceProductVO 목록 (총 " + results.size() + "개):");
        results.forEach(product -> {
            System.out.println("  - 상품 ID: " + product.getProductId() + ", 상품명: " + product.getProductName() + ", 보험사: " + product.getProviderName());
            System.out.println("    보장한도: " + product.getCoverageLimit() + ", 비고: " + product.getNote() + ", 자기부담금: " + product.getMyMoney());
            System.out.println("    점수: 가격(" + product.getPriceCompetitivenessScore() + "), 보장한도(" + product.getCoverageLimitScore() + "), 보장범위(" + product.getCoverageScopeScore() + "), 자기부담금(" + product.getDeductibleScore() + "), 환급범위(" + product.getRefundScopeScore() + ")");
        });
        System.out.println("-------------------------------------------------------------------");
    }
}