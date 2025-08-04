package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.mapper.SavingsRecommendationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavingsRecommendationServiceImplTest {

    @Mock
    private SavingsRecommendationMapper recommendationMapper;

    @InjectMocks
    private SavingsRecommendationServiceImpl savingsRecommendationService;

    private UserIncomeInfoVO mockUserInfo;
    private List<SavingsProductVO> mockProducts;
    private final String userId = "testUser-mockito";

    @BeforeEach
    void setUp() {
        // given: 테스트용 가짜 객체 설정
        mockUserInfo = new UserIncomeInfoVO();
        // ReflectionTestUtils: setter가 없는 필드에 값을 강제로 주입하기 위한 스프링 테스트 유틸리티
        ReflectionTestUtils.setField(mockUserInfo, "userId", userId);
        ReflectionTestUtils.setField(mockUserInfo, "selectedWonnabeIds", "[1,7]");
        ReflectionTestUtils.setField(mockUserInfo, "incomeSourceType", "급여");
        ReflectionTestUtils.setField(mockUserInfo, "incomeEmploymentStatus", "정규직");

        SavingsProductVO product1 = new SavingsProductVO();
        product1.setProductId(1L); product1.setProductName("상품A");
        product1.setInterestRateScore(10); product1.setCompoundInterestScore(5);
        product1.setPreferentialScore(3); product1.setPenaltyScore(8);
        product1.setLimitScore(8);
        product1.setBaseRate(1.0f);
        product1.setMaxRate(1.5f);

        SavingsProductVO product2 = new SavingsProductVO();
        product2.setProductId(2L); product2.setProductName("상품B");
        product2.setInterestRateScore(5); product2.setCompoundInterestScore(8);
        product2.setPreferentialScore(10); product2.setPenaltyScore(3);
        product2.setLimitScore(4);
        product2.setBaseRate(2.0f);
        product2.setMaxRate(2.5f);

        mockProducts = Arrays.asList(product1, product2);
    }

    @Test
    @DisplayName("[성공] 사용자 페르소나별 가중치에 따라 상품을 정확히 추천")
    void recommendSavings_성공() {
        // given: 가짜 매퍼의 동작 정의
        int topN = 1;
        when(recommendationMapper.getUserIncomeInfo(userId)).thenReturn(mockUserInfo);
        when(recommendationMapper.getAllSavingsScores()).thenReturn(mockProducts);

        // when: 서비스 메서드 호출
        SavingsRecommendationResponseDTO response = savingsRecommendationService.recommendSavings(userId, topN);

        // then: 결과 검증
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(2, response.getRecommendationsByPersona().size());

        assertEquals("상품A", response.getRecommendationsByPersona().get(0).getProducts().get(0).getProductName());
        assertEquals("상품B", response.getRecommendationsByPersona().get(1).getProducts().get(0).getProductName());
        System.out.println(" --- [SavingsRecommendationServiceImplTest] 적금 추천 서비스 결과 ---");
        System.out.println("사용자 ID: " + response.getUserId());
        response.getRecommendationsByPersona().forEach(personaRec -> {
            System.out.println("  페르소나 ID: " + personaRec.getPersonaId() + " (" + personaRec.getPersonaName() + ")");
            System.out.println("    추천 상품 목록:");
            if (personaRec.getProducts().isEmpty()) {
                System.out.println("      (추천 상품 없음)");
            } else {
                personaRec.getProducts().forEach(product -> {
                    System.out.println("      - 상품 ID: " + product.getProductId() + ", 상품명: " + product.getProductName() + ", 은행: " + product.getBankName());
                    System.out.println("        기본 금리: " + product.getBaseRate() + ", 최고 금리: " + product.getMaxRate() + ", 총 점수: " + product.getTotalScore());
                });
            }
        });
        System.out.println("-------------------------------------------------------------------");
    }
}
