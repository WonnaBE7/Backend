package com.wonnabe.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.BasicUserInfoDTO;
import com.wonnabe.product.dto.InsuranceProductDetailResponseDTO;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;
import com.wonnabe.product.mapper.ProductDetailMapper;
import com.wonnabe.product.mapper.UserInsuranceMapper;
import com.wonnabe.product.mapper.UserSavingsMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceImplTest {

    @InjectMocks
    private ProductDetailServiceImpl productService;

    @Mock
    private ProductDetailMapper productDetailMapper;

    @Mock
    private UserSavingsMapper userSavingsMapper;

    @Mock
    private SavingsRecommendationService savingsRecommendationService;

    @Mock
    private InsuranceRecommendationService insuranceRecommendationService;

    @Mock
    private UserInsuranceMapper userInsuranceMapper;

    @Mock
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    @DisplayName("예적금 상세 조회 (로그인, 찜 O)")
    void getSavingProductDetail_loggedIn_wished() {
        // given
        String productId = "1110";
        String userId = "test-user-uuid";
        String favoriteJson = "[" + productId + "]";

        SavingsProductVO product = SavingsProductVO.builder().productId(Long.valueOf(productId)).mtrtInt("").build();
        BasicUserInfoDTO basicUserInfo = BasicUserInfoDTO.builder().userId(userId).nowMeId(1).favoriteProductsByType(favoriteJson).build();

        given(productDetailMapper.findSavingProductById(productId)).willReturn(product);
        given(productDetailMapper.findBasicUserInfoById(userId)).willReturn(basicUserInfo);
        given(objectMapper.readValue(eq(favoriteJson), any(TypeReference.class))).willReturn(List.of(Long.valueOf(productId)));
        given(savingsRecommendationService.getPersonaWeights()).willReturn(Map.of(1, new double[]{0.2,0.2,0.2,0.2,0.2}));

        // when
        SavingsProductDetailResponseDto result = productService.getSavingProductDetail(productId, userId, null);

        // then
        assertNotNull(result);
        assertTrue(result.getProductInfo().isWished());
    }

    @SneakyThrows
    @Test
    @DisplayName("예적금 상세 조회 (로그인, wannabeId 사용)")
    void getSavingProductDetail_loggedIn_withWannabeId() {
        // given
        String productId = "1110";
        String userId = "test-user-uuid";
        Integer wannabeId = 5;
        double[] wannabeWeights = {0.5, 0.1, 0.1, 0.1, 0.2};
        String favoriteJson = "[]";

        SavingsProductVO product = SavingsProductVO.builder().productId(Long.valueOf(productId)).mtrtInt("").build();
        BasicUserInfoDTO basicUserInfo = BasicUserInfoDTO.builder().userId(userId).nowMeId(1).favoriteProductsByType(favoriteJson).build();

        given(productDetailMapper.findSavingProductById(productId)).willReturn(product);
        given(productDetailMapper.findBasicUserInfoById(userId)).willReturn(basicUserInfo);
        given(objectMapper.readValue(eq(favoriteJson), any(TypeReference.class))).willReturn(Collections.emptyList());
        given(savingsRecommendationService.getPersonaWeights()).willReturn(Map.of(wannabeId, wannabeWeights));
        given(savingsRecommendationService.calculateScore(any(), any())).willReturn(95.0);

        // when
        SavingsProductDetailResponseDto result = productService.getSavingProductDetail(productId, userId, wannabeId);

        // then
        assertNotNull(result);
        assertEquals(95.0, result.getProductInfo().getScore());
        verify(savingsRecommendationService).calculateScore(any(), any(double[].class));
    }

    @Test
    @DisplayName("예적금 상세 조회 (비로그인)")
    void getSavingProductDetail_guest() {
        // given
        String productId = "1110";
        SavingsProductVO product = SavingsProductVO.builder().productId(Long.valueOf(productId)).mtrtInt("").build();
        given(productDetailMapper.findSavingProductById(productId)).willReturn(product);

        // when
        SavingsProductDetailResponseDto result = productService.getSavingProductDetail(productId, null, null);

        // then
        assertNotNull(result);
        assertFalse(result.getProductInfo().isWished());
        assertEquals(0.0, result.getProductInfo().getScore());
        assertTrue(result.getComparisonChart().isEmpty());
    }

    @SneakyThrows
    @Test
    @DisplayName("보험 상세 조회 (로그인, 찜 O)")
    void getInsuranceProductDetail_loggedIn_wished() {
        // given
        String productId = "3001";
        String userId = "test-user-uuid";
        String favoriteJson = "[" + productId + "]";

        InsuranceProductVO product = InsuranceProductVO.builder().productId(Long.valueOf(productId)).femalePremium(BigDecimal.ZERO).malePremium(BigDecimal.ZERO).build();
        BasicUserInfoDTO basicUserInfo = BasicUserInfoDTO.builder().userId(userId).nowMeId(1).favoriteProductsByType(favoriteJson).build();

        given(productDetailMapper.findInsuranceProductById(productId)).willReturn(product);
        given(productDetailMapper.findBasicUserInfoById(userId)).willReturn(basicUserInfo);
        given(objectMapper.readValue(eq(favoriteJson), any(TypeReference.class))).willReturn(List.of(Long.valueOf(productId)));
        given(insuranceRecommendationService.getPersonaWeights()).willReturn(Map.of(1, new double[]{0.2,0.2,0.2,0.2,0.2}));

        // when
        InsuranceProductDetailResponseDTO result = productService.getInsuranceProductDetail(productId, userId, null);

        // then
        assertNotNull(result);
        assertTrue(result.getProductInfo().isWished());
    }

    @SneakyThrows
    @Test
    @DisplayName("보험 상세 조회 (로그인, wannabeId 사용)")
    void getInsuranceProductDetail_loggedIn_withWannabeId() {
        // given
        String productId = "3001";
        String userId = "test-user-uuid";
        Integer wannabeId = 3;
        double[] wannabeWeights = {0.1, 0.4, 0.2, 0.2, 0.1};
        String favoriteJson = "[]";

        InsuranceProductVO product = InsuranceProductVO.builder().productId(Long.valueOf(productId)).femalePremium(BigDecimal.ZERO).malePremium(BigDecimal.ZERO).build();
        BasicUserInfoDTO basicUserInfo = BasicUserInfoDTO.builder().userId(userId).nowMeId(1).favoriteProductsByType(favoriteJson).build();

        given(productDetailMapper.findInsuranceProductById(productId)).willReturn(product);
        given(productDetailMapper.findBasicUserInfoById(userId)).willReturn(basicUserInfo);
        given(objectMapper.readValue(eq(favoriteJson), any(TypeReference.class))).willReturn(Collections.emptyList());
        given(insuranceRecommendationService.getPersonaWeights()).willReturn(Map.of(wannabeId, wannabeWeights));
        given(insuranceRecommendationService.calculateScore(any(), any())).willReturn(88.0);

        // when
        InsuranceProductDetailResponseDTO result = productService.getInsuranceProductDetail(productId, userId, wannabeId);

        // then
        assertNotNull(result);
        assertEquals(88.0, result.getProductInfo().getScore());
        verify(insuranceRecommendationService).calculateScore(any(), any(double[].class));
    }

    @Test
    @DisplayName("보험 상세 조회 (비로그인)")
    void getInsuranceProductDetail_guest() {
        // given
        String productId = "3001";
        InsuranceProductVO product = InsuranceProductVO.builder().productId(Long.valueOf(productId)).femalePremium(BigDecimal.ZERO).malePremium(BigDecimal.ZERO).build();
        given(productDetailMapper.findInsuranceProductById(productId)).willReturn(product);

        // when
        InsuranceProductDetailResponseDTO result = productService.getInsuranceProductDetail(productId, null, null);

        // then
        assertNotNull(result);
        assertFalse(result.getProductInfo().isWished());
        assertEquals(0.0, result.getProductInfo().getScore());
        assertTrue(result.getComparisonChart().isEmpty());
    }
}
