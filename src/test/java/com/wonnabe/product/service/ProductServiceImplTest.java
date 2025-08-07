package com.wonnabe.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.BasicUserInfo;
import com.wonnabe.product.dto.SavingsProductDetailResponseDto;
import com.wonnabe.product.mapper.ProductMapper;
import com.wonnabe.product.mapper.UserSavingsMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserSavingsMapper userSavingsMapper;

    @Mock
    private SavingsRecommendationService savingsRecommendationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @SneakyThrows
    @Test
    @DisplayName("예적금 상품 상세 정보 조회 - 사용자가 찜한 상품일 경우")
    void getSavingProductDetail_whenProductIsWished() {
        // given
        String productId = "1110";
        String userId = "test-user-uuid";

        // CustomUser 객체를 생성하여 반환하도록 모킹
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("password");
        CustomUser customUser = new CustomUser(userVO);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(customUser);

        SavingsProductVO product = SavingsProductVO.builder()
                .productId(Long.valueOf(productId))
                .productName("테스트 적금")
                .bankName("테스트 은행")
                .baseRate(3.5f).maxRate(4.0f).prefer("우대조건 테스트")
                .mtrtInt("만기 후 이율 정보1,만기 후 이율 정보2").maxJoinPeriod(36)
                .interestRateScore(80).compoundInterestScore(70).preferentialScore(90).penaltyScore(60).limitScore(85)
                .build();

        BasicUserInfo basicUserInfo = BasicUserInfo.builder()
                .userId(userId)
                .nowMeId(1)
                .favoriteProductsByType("[" + productId + ", \"1112\"]")
                .build();

        given(objectMapper.readValue(anyString(), any(TypeReference.class))).willAnswer(invocation -> {
            String json = invocation.getArgument(0);
            if (json.contains(productId)) { // Check if the productId is in the JSON string
                return List.of(Long.valueOf(productId), 1112L);
            }
            return Collections.emptyList(); // Default for other cases, or throw an exception if expected
        });

        given(productMapper.findSavingProductById(anyString())).willReturn(product);
        given(productMapper.findBasicUserInfoById(anyString())).willReturn(basicUserInfo);

        double[] weights = {0.3, 0.1, 0.2, 0.3, 0.2};
        given(savingsRecommendationService.getPersonaWeights()).willReturn(Map.of(1, weights));
        given(savingsRecommendationService.calculateScore(any(SavingsProductVO.class), any(double[].class))).willReturn(78.5);

        given(userSavingsMapper.findAllByUserId(anyString())).willReturn(Collections.emptyList());

        // when
        SavingsProductDetailResponseDto result = productService.getSavingProductDetail(productId);

        // then
        assertNotNull(result, "결과 DTO는 null이 아니어야 합니다.");

        // ProductInfo 검증
        var productInfo = result.getProductInfo();
        assertNotNull(productInfo, "ProductInfo는 null이 아니어야 합니다.");
        assertEquals(productId, productInfo.getProductId(), "상품 ID가 일치해야 합니다.");
        assertEquals("테스트 적금", productInfo.getProductName(), "상품명이 일치해야 합니다.");
        assertEquals(78, productInfo.getMatchScore(), "Match Score가 일치해야 합니다.");
        assertTrue(productInfo.isWished(), "찜한 상품이므로 isWished는 true여야 합니다.");

        // ComparisonChart 검증
        assertTrue(result.getComparisonChart().isEmpty(), "비교 차트 정보는 비어있어야 합니다.");

        // MaturityInfo 검증
        var maturityInfo = result.getMaturityInfo();
        assertNotNull(maturityInfo, "만기 정보는 null이 아니어야 합니다.");
        assertEquals("36개월", maturityInfo.getMaxJoinPeroid(), "최대 가입 기간이 일치해야 합니다.");
        assertEquals(List.of("만기 후 이율 정보1", "만기 후 이율 정보2"), maturityInfo.getContent(), "만기 후 금리 내용이 일치해야 합니다.");
    }

    @SneakyThrows
    @Test
    @DisplayName("예적금 상품 상세 정보 조회 - 사용자가 찜하지 않은 상품일 경우")
    void getSavingProductDetail_whenProductIsNotWished() {
        // given
        String productId = "1110";
        String userId = "test-user-uuid";

        // CustomUser 객체를 생성하여 반환하도록 모킹
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("password");
        CustomUser customUser = new CustomUser(userVO);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(customUser);

        SavingsProductVO product = SavingsProductVO.builder().productId(Long.valueOf(productId)).mtrtInt("").build();

        BasicUserInfo basicUserInfo = BasicUserInfo.builder()
                .userId(userId)
                .nowMeId(1)
                .favoriteProductsByType("[\"1112\", \"1113\"]")
                .build();

        given(objectMapper.readValue(anyString(), any(TypeReference.class))).willAnswer(invocation -> {
            String json = invocation.getArgument(0);
            if (json.contains(productId)) { // Check if the productId is in the JSON string
                return List.of(Long.valueOf(productId), 1112L);
            } else {
                return List.of(1112L, 1113L);
            }
        });

        given(productMapper.findSavingProductById(anyString())).willReturn(product);
        given(productMapper.findBasicUserInfoById(anyString())).willReturn(basicUserInfo);
        given(savingsRecommendationService.getPersonaWeights()).willReturn(Map.of(1, new double[]{0.2,0.2,0.2,0.2,0.2}));
        given(savingsRecommendationService.calculateScore(any(), any())).willReturn(0.0);
        given(userSavingsMapper.findAllByUserId(anyString())).willReturn(Collections.emptyList());

        // when
        SavingsProductDetailResponseDto result = productService.getSavingProductDetail(productId);

        // then
        assertNotNull(result, "결과 DTO는 null이 아니어야 합니다.");
        assertFalse(result.getProductInfo().isWished(), "찜하지 않은 상품이므로 isWished는 false여야 합니다.");
    }
}
