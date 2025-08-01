package com.wonnabe.product.controller;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO.PersonaRecommendation;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO.RecommendedSavings;
import com.wonnabe.product.service.SavingsRecommendationService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.wonnabe.common.config.ServletConfig;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Log4j2
@ActiveProfiles("test")
class RecommendSavingsControllerTest {

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    @Autowired
    private RecommendSavingsController recommendSavingsController; // 실제 컨트롤러 주입

    @Mock
    private SavingsRecommendationService mockSavingsRecommendationService; // 서비스 Mock 객체

    // 테스트용 사용자 데이터
    private final String userId = "a1b2c3d4-e5f6-7890-ab12-cd34ef56gh78";

    @BeforeEach
    public void setup() throws Exception {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        // 가짜 유저 데이터 생성
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("dummy-password");
        userVO.setName("테스트 유저");

        // SecurityContext에 사용자 인증 정보를 저장함
        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 컨트롤러의 실제 서비스를 Mock으로 교체
        ReflectionTestUtils.setField(recommendSavingsController, "savingsRecommendationService", mockSavingsRecommendationService);
        
        // Mock 초기화 - 이전 테스트의 설정을 모두 제거
        reset(mockSavingsRecommendationService);

        log.info("테스트 사용자 설정 완료 - userId: {}", userId);
    }

    

    /**
     * 테스트용 Mock 응답 데이터 생성
     */
    private SavingsRecommendationResponseDTO createMockResponse(String userId, int topN) {
        SavingsRecommendationResponseDTO response = new SavingsRecommendationResponseDTO();
        response.setUserId(userId);
        response.setRecommendationsByPersona(new ArrayList<>());

        // 페르소나별 추천 데이터 생성
        PersonaRecommendation personaRec = new PersonaRecommendation();
        personaRec.setPersonaId(1);
        personaRec.setPersonaName("자린고비형");
        personaRec.setProducts(new ArrayList<>());

        // 추천 상품 생성
        for (int i = 1; i <= topN; i++) {
            RecommendedSavings savings = new RecommendedSavings();
            savings.setProductId("TEST00" + i);
            savings.setProductName("테스트 적금 상품 " + i);
            savings.setBankName("테스트은행" + i);
            savings.setBaseRate(3.0f + i * 0.1f);
            savings.setMaxRate(4.0f + i * 0.1f);
            savings.setTotalScore(85.0 + i);

            personaRec.getProducts().add(savings);
        }

        response.getRecommendationsByPersona().add(personaRec);
        return response;
    }

    @Test
    @DisplayName("예적금 추천 API 성공 - 기본 파라미터")
    void recommendSavings_success() throws Exception {
        // given - Mock 서비스 응답 데이터 설정
        SavingsRecommendationResponseDTO mockResponse = createMockResponse(userId, 5);

        // Mock 설정 확인
        log.info("Mock 설정 시작 - userId: {}, topN: 5", userId);
        when(mockSavingsRecommendationService.recommendSavings(eq(userId), eq(5)))
                .thenReturn(mockResponse);

        log.info("Mock 설정 완료");

        try {
            // when & then
            var result = mockMvc.perform(get("/api/recommendations/savings"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.recommendationsByPersona").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona").isArray())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].personaId").value(1))
                    .andExpect(jsonPath("$.recommendationsByPersona[0].personaName").value("자린고비형"))
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products").isArray())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products.length()").value(5));

            // Mock 호출 검증
            verify(mockSavingsRecommendationService, times(1)).recommendSavings(eq(userId), eq(5));
            log.info("기본 파라미터 테스트 성공");

            if (result.andReturn().getResolvedException() != null) {
                log.error("컨트롤러에서 발생한 예외: ", result.andReturn().getResolvedException());
            }
        } catch (Exception e) {
            log.error("테스트 실패 - 상세 에러 정보: ", e);
            throw e;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("예적금 추천 API 성공 - topN 파라미터 포함")
    void recommendSavings_success_withTopN() throws Exception {
        // given
        int topN = 3;
        SavingsRecommendationResponseDTO mockResponse = createMockResponse(userId, topN);

        when(mockSavingsRecommendationService.recommendSavings(eq(userId), eq(topN)))
                .thenReturn(mockResponse);

        try {
            // when & then
            var result = mockMvc.perform(get("/api/recommendations/savings")
                            .param("topN", String.valueOf(topN)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.recommendationsByPersona").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products.length()").value(topN))
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].productId").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].productName").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].bankName").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].baseRate").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].maxRate").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].totalScore").exists());

            verify(mockSavingsRecommendationService, times(1)).recommendSavings(eq(userId), eq(topN));
            log.info("topN 파라미터 테스트 성공");

            if (result.andReturn().getResolvedException() != null) {
                log.error("컨트롤러에서 발생한 예외: ", result.andReturn().getResolvedException());
            }
        } catch (Exception e) {
            log.error("테스트 실패 - 상세 에러 정보 (recommendSavings_success_withTopN): ", e);
            throw e;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("예적금 추천 API 실패 - 서비스에서 예외 발생")
    void recommendSavings_serviceException() throws Exception {
        // given - Mock 서비스에서 예외 발생하도록 설정
        when(mockSavingsRecommendationService.recommendSavings(anyString(), anyInt()))
                .thenThrow(new RuntimeException("서비스 에러 테스트"));

        try {
            // when & then
            mockMvc.perform(get("/api/recommendations/savings"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError()); // 500 에러 예상

            verify(mockSavingsRecommendationService, times(1)).recommendSavings(anyString(), anyInt());
            log.info("서비스 예외 테스트 성공");
        } catch (Exception e) {
            log.error("테스트 실패 - 서비스 예외 테스트: ", e);
            throw e;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("예적금 추천 API 성공 - 빈 결과")
    void recommendSavings_emptyResult() throws Exception {
        // given - 빈 결과 반환
        SavingsRecommendationResponseDTO emptyResponse = new SavingsRecommendationResponseDTO();
        emptyResponse.setUserId(userId);
        emptyResponse.setRecommendationsByPersona(new ArrayList<>());

        when(mockSavingsRecommendationService.recommendSavings(eq(userId), anyInt()))
                .thenReturn(emptyResponse);

        try {
            // when & then
            mockMvc.perform(get("/api/recommendations/savings"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.recommendationsByPersona").exists())
                    .andExpect(jsonPath("$.recommendationsByPersona").isArray())
                    .andExpect(jsonPath("$.recommendationsByPersona.length()").value(0));

            verify(mockSavingsRecommendationService, times(1)).recommendSavings(eq(userId), anyInt());
            log.info("빈 결과 테스트 성공");
        } catch (Exception e) {
            log.error("테스트 실패 - 빈 결과 테스트: ", e);
            throw e;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}