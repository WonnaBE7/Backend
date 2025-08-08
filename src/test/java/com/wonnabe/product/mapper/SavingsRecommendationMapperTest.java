package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.SavingsProductVO;
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

class SavingsRecommendationMapperTest {

    @Autowired
    private SavingsRecommendationMapper savingsRecommendationMapper;

    private final String userId = "a1b2c3d4-e5f6-7890-ab12-cd34ef56gh78";

    @Test
    @DisplayName("[성공] 사용자 ID로 소득 및 페르소나 정보를 DB에서 조회")
    void getUserIncomeInfo_성공() {
        // when
        UserIncomeInfoVO result = savingsRecommendationMapper.getUserIncomeInfo(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getPersonaIds());
        assertFalse(result.getPersonaIds().isEmpty());
        System.out.println("\n--- [SavingsRecommendationMapperTest] 사용자 소득 및 페르소나 정보 조회 결과 ---");
        System.out.println("조회된 UserIncomeInfoVO 객체:");
        System.out.println("  - 사용자 ID: " + result.getUserId());
        System.out.println("  - 선택된 워너비 ID 문자열: " + result.getSelectedWonnabeIds());
        System.out.println("  - 파싱된 페르소나 ID 목록: " + result.getPersonaIds());
        System.out.println("  - 소득원 타입: " + result.getIncomeSourceType());
        System.out.println("  - 고용 상태: " + result.getIncomeEmploymentStatus());
        System.out.println("-------------------------------------------------------------------");
    }

    @Test
    @DisplayName("[성공] 모든 예적금 상품과 점수 정보를 DB에서 조회")
    void getAllSavingsScores_성공() {
        // when
        List<SavingsProductVO> results = savingsRecommendationMapper.getAllSavingsScores();

        // then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertNotNull(results.get(0).getScoreInterestRate());
        System.out.println("\n--- [SavingsRecommendationMapperTest] 모든 예적금 상품 및 점수 조회 결과 ---");
        System.out.println("조회된 SavingsProductVO 목록 (총 " + results.size() + "개):");
        results.forEach(product -> {
            System.out.println("  - 상품 ID: " + product.getProductId() + ", 상품명: " + product.getProductName() + ", 은행: " + product.getBankName());
            System.out.println("    금리: " + product.getBaseRate() + " ~ " + product.getMaxRate() + ", 최대한도: " + product.getMaxAmount());
            System.out.println("    점수: 금리(" + product.getScoreInterestRate() + "), 복리(" + product.getScoreInterestType() + "), 우대(" + product.getScorePreferentialCondition() + "), 중도해지(" + product.getScoreCancelBenefit() + "), 한도(" + product.getScoreMaxAmount() + ")");
        });
        System.out.println("-------------------------------------------------------------------");
    }
}