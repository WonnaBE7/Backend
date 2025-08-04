package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserIncomeInfoVO;
import com.wonnabe.product.dto.InsuranceRecommendationResponseDTO;
import com.wonnabe.product.mapper.InsuranceRecommendationMapper;
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
class InsuranceRecommendationServiceImplTest {

    @Mock
    private InsuranceRecommendationMapper recommendationMapper;

    @InjectMocks
    private InsuranceRecommendationServiceImpl insuranceRecommendationService;

    private UserIncomeInfoVO mockUserInfo;
    private List<InsuranceProductVO> mockProducts;
    private final String userId = "testUser-mockito";

    @BeforeEach
    void setUp() {
        // given: 테스트용 가짜 객체 설정
        mockUserInfo = new UserIncomeInfoVO();
        // ReflectionTestUtils: setter가 없는 필드에 값을 강제로 주입하기 위한 스프링 테스트 유틸리티
        ReflectionTestUtils.setField(mockUserInfo, "userId", userId);
        ReflectionTestUtils.setField(mockUserInfo, "selectedWonnabeIds", "[1,7]"); // 예: 자린고비형, 미래 준비형
        ReflectionTestUtils.setField(mockUserInfo, "incomeSourceType", "급여");
        ReflectionTestUtils.setField(mockUserInfo, "incomeEmploymentStatus", "정규직");
        // 보험 추천을 위한 건강/생활습관 정보
        ReflectionTestUtils.setField(mockUserInfo, "smokingStatus", "N");
        ReflectionTestUtils.setField(mockUserInfo, "familyMedicalHistory", "없음");
        ReflectionTestUtils.setField(mockUserInfo, "pastMedicalHistory", "N");
        ReflectionTestUtils.setField(mockUserInfo, "exerciseFrequency", "매일");
        ReflectionTestUtils.setField(mockUserInfo, "drinkingFrequency", "안함");

        InsuranceProductVO product1 = new InsuranceProductVO();
        product1.setProductId(1L); product1.setProductName("보험A"); product1.setProviderName("보험사A");
        product1.setPriceCompetitivenessScore(10.0f); product1.setCoverageLimitScore(5.0f);
        product1.setCoverageScopeScore(8.0f); product1.setDeductibleScore(7.0f);
        product1.setRefundScopeScore(6.0f);
        product1.setCoverageLimit("1억"); product1.setNote("특약1"); product1.setMyMoney("10000");

        InsuranceProductVO product2 = new InsuranceProductVO();
        product2.setProductId(2L); product2.setProductName("보험B"); product2.setProviderName("보험사B");
        product2.setPriceCompetitivenessScore(5.0f); product2.setCoverageLimitScore(10.0f);
        product2.setCoverageScopeScore(7.0f); product2.setDeductibleScore(8.0f);
        product2.setRefundScopeScore(9.0f);
        product2.setCoverageLimit("2억"); product2.setNote("특약2"); product2.setMyMoney("20000");

        mockProducts = Arrays.asList(product1, product2);
    }

    @Test
    @DisplayName("[성공] 사용자 페르소나 및 건강/생활습관에 따라 보험 상품을 정확히 추천")
    void recommendInsurance_성공() {
        // given: 가짜 매퍼의 동작 정의
        int topN = 1;
        when(recommendationMapper.getUserHealthInfo(userId)).thenReturn(mockUserInfo);
        when(recommendationMapper.getAllInsuranceScores()).thenReturn(mockProducts);

        // when: 서비스 메서드 호출
        InsuranceRecommendationResponseDTO response = insuranceRecommendationService.recommendInsurance(userId, topN);

        // then: 결과 검증
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertFalse(response.getRecommendationsByPersona().isEmpty());

        // 첫 번째 페르소나 (자린고비형)에 대한 추천 검증
        InsuranceRecommendationResponseDTO.PersonaRecommendation persona1Rec = response.getRecommendationsByPersona().get(0);
        assertEquals(1, persona1Rec.getPersonaId());
        assertEquals("자린고비형", persona1Rec.getPersonaName());
        assertFalse(persona1Rec.getProducts().isEmpty());
        assertEquals(topN, persona1Rec.getProducts().size());
        assertEquals("보험A", persona1Rec.getProducts().get(0).getProductName());

        // 두 번째 페르소나 (미래 준비형)에 대한 추천 검증
        InsuranceRecommendationResponseDTO.PersonaRecommendation persona2Rec = response.getRecommendationsByPersona().get(1);
        assertEquals(7, persona2Rec.getPersonaId());
        assertEquals("미래 준비형", persona2Rec.getPersonaName());
        assertFalse(persona2Rec.getProducts().isEmpty());
        assertEquals(topN, persona2Rec.getProducts().size());
        assertEquals("보험B", persona2Rec.getProducts().get(0).getProductName());

        System.out.println(" --- [InsuranceRecommendationServiceImplTest] 보험 추천 서비스 결과 ---");
        System.out.println("사용자 ID: " + response.getUserId());
        response.getRecommendationsByPersona().forEach(personaRec -> {
            System.out.println("  페르소나 ID: " + personaRec.getPersonaId() + " (" + personaRec.getPersonaName() + ")");
            System.out.println("    추천 상품 목록:");
            if (personaRec.getProducts().isEmpty()) {
                System.out.println("      (추천 상품 없음)");
            } else {
                personaRec.getProducts().forEach(product -> {
                    System.out.println("      - 상품 ID: " + product.getProductId() + ", 상품명: " + product.getProductName() + ", 보험사: " + product.getProviderName());
                    System.out.println("        보장한도: " + product.getCoverageLimit() + ", 비고: " + product.getNote() + ", 자기부담금: " + product.getMyMoney() + ", 총 점수: " + product.getTotalScore());
                    System.out.println("        상품 타입: " + product.getProductType());
                });
            }
        });
        System.out.println("-------------------------------------------------------------------");
    }
}