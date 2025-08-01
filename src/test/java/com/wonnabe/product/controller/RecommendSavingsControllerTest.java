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
     * @param userId 사용자 ID
     * @param topN 추천할 상품의 개수
     * @return Mock SavingsRecommendationResponseDTO 객체
     */
    private SavingsRecommendationResponseDTO createMockResponse(String userId, int topN) {
        SavingsRecommendationResponseDTO response = new SavingsRecommendationResponseDTO();
        response.setUserId(userId);
        response.setRecommendationsByPersona(new ArrayList<>());

        log.info("--- Mock 추천 응답 데이터 생성 시작 (userId: {}, topN: {}) ---", userId, topN);

        // 페르소나별 추천 데이터 생성
        PersonaRecommendation personaRec = new PersonaRecommendation();
        personaRec.setPersonaId(1);
        personaRec.setPersonaName("자린고비형");
        personaRec.setProducts(new ArrayList<>());

        log.info("  - 페르소나: {} (ID: {})", personaRec.getPersonaName(), personaRec.getPersonaId());
        log.info("  - 생성될 추천 상품 개수: {}", topN);

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
            log.info("    > 상품 추가: {} (ID: {})", savings.getProductName(), savings.getProductId());
        }

        response.getRecommendationsByPersona().add(personaRec);
        log.info("--- Mock 추천 응답 데이터 생성 완료 ---");
        return response;
    }

    @Test
    @DisplayName("예적금 추천 API 성공 - 기본 파라미터")
    void recommendSavings_success() throws Exception {
        // given - Mock 서비스 응답 데이터 설정
        // userId와 topN=5로 Mock 응답 데이터를 생성합니다.
        SavingsRecommendationResponseDTO mockResponse = createMockResponse(userId, 5);

        // Mock 서비스의 recommendSavings 메서드가 특정 userId와 topN=5로 호출될 때
        // 위에서 생성한 mockResponse를 반환하도록 설정합니다.
        log.info("--- [recommendSavings_success] Mock 서비스 설정 시작 ---");
        log.info("  - userId: {}, topN: 5", userId);
        log.info("  - Mock 서비스가 반환할 응답 데이터: {}", mockResponse);
        when(mockSavingsRecommendationService.recommendSavings(eq(userId), eq(5)))
                .thenReturn(mockResponse);
        log.info("--- [recommendSavings_success] Mock 서비스 설정 완료 ---");

        try {
            // when & then - API 호출 및 응답 검증
            log.info("--- [recommendSavings_success] API 호출 시작 ---");
            // /api/recommendations/savings 엔드포인트로 GET 요청을 보냅니다.
            var result = mockMvc.perform(get("/api/recommendations/savings"))
                    .andDo(print()) // MockMvc 요청/응답 전체 내용을 콘솔에 출력합니다.
                    .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 검증합니다.
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) // 응답 Content-Type이 application/json;charset=UTF-8인지 검증합니다.
                    .andExpect(jsonPath("$.userId").value(userId)) // 응답 JSON의 userId 필드 값이 예상 userId와 일치하는지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona").exists()) // recommendationsByPersona 필드가 존재하는지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona").isArray()) // recommendationsByPersona가 배열인지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona[0].personaId").value(1)) // 첫 번째 페르소나의 ID가 1인지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona[0].personaName").value("자린고비형")) // 첫 번째 페르소나의 이름이 "자린고비형"인지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products").isArray()) // 첫 번째 페르소나의 products가 배열인지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products.length()").value(5)); // 첫 번째 페르소나의 추천 상품 개수가 5개인지 검증합니다.

            log.info("--- [recommendSavings_success] API 호출 및 응답 검증 완료 ---");
            log.info("  - 예상 HTTP 상태: 200 OK, 실제 HTTP 상태: {}", result.andReturn().getResponse().getStatus());
            log.info("  - 응답 본문: {}", result.andReturn().getResponse().getContentAsString());

            // Mock 호출 검증
            // mockSavingsRecommendationService의 recommendSavings 메서드가 정확히 한 번 호출되었는지,
            // 그리고 호출 시 userId와 topN=5 인자가 사용되었는지 검증합니다.
            verify(mockSavingsRecommendationService, times(1)).recommendSavings(eq(userId), eq(5));
            log.info("--- [recommendSavings_success] 기본 파라미터 테스트 성공 ---");

            // 컨트롤러에서 발생한 예외가 있다면 로그 출력 (디버깅용)
            if (result.andReturn().getResolvedException() != null) {
                log.error("컨트롤러에서 발생한 예외: ", result.andReturn().getResolvedException());
            }
        } catch (Exception e) {
            // 테스트 실패 시 상세 에러 정보 로그 출력
            log.error("--- [recommendSavings_success] 테스트 실패 - 상세 에러 정보: ---", e);
            throw e; // 예외를 다시 던져 테스트 실패를 알립니다.
        } finally {
            // 각 테스트 메서드 실행 후 SecurityContext를 초기화하여 테스트 간 독립성을 유지합니다.
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("예적금 추천 API 성공 - topN 파라미터 포함")
    void recommendSavings_success_withTopN() throws Exception {
        // given - Mock 서비스 응답 데이터 설정
        int topN = 3; // 추천할 상품 개수를 3으로 설정합니다.
        // userId와 topN=3으로 Mock 응답 데이터를 생성합니다.
        SavingsRecommendationResponseDTO mockResponse = createMockResponse(userId, topN);

        // Mock 서비스의 recommendSavings 메서드가 특정 userId와 topN=3으로 호출될 때
        // 위에서 생성한 mockResponse를 반환하도록 설정합니다.
        log.info("--- [recommendSavings_success_withTopN] Mock 서비스 설정 시작 ---");
        log.info("  - userId: {}, topN: {}", userId, topN);
        log.info("  - Mock 서비스가 반환할 응답 데이터: {}", mockResponse);
        when(mockSavingsRecommendationService.recommendSavings(eq(userId), eq(topN)))
                .thenReturn(mockResponse);
        log.info("--- [recommendSavings_success_withTopN] Mock 서비스 설정 완료 ---");

        try {
            // when & then - API 호출 및 응답 검증
            log.info("--- [recommendSavings_success_withTopN] API 호출 시작 ---");
            // /api/recommendations/savings 엔드포인트로 GET 요청을 보내고 topN 파라미터를 포함합니다.
            var result = mockMvc.perform(get("/api/recommendations/savings")
                            .param("topN", String.valueOf(topN))) // "topN" 파라미터에 topN 값을 문자열로 전달합니다.
                    .andDo(print()) // MockMvc 요청/응답 전체 내용을 콘솔에 출력합니다.
                    .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 검증합니다.
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)) // 응답 Content-Type이 application/json;charset=UTF-8인지 검증합니다.
                    .andExpect(jsonPath("$.userId").value(userId)) // 응답 JSON의 userId 필드 값이 예상 userId와 일치하는지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona").exists()) // recommendationsByPersona 필드가 존재하는지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products.length()").value(topN)) // 첫 번째 페르소나의 추천 상품 개수가 topN과 일치하는지 검증합니다.
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].productId").exists()) // 첫 번째 추천 상품의 productId 필드 존재 여부 검증
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].productName").exists()) // 첫 번째 추천 상품의 productName 필드 존재 여부 검증
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].bankName").exists()) // 첫 번째 추천 상품의 bankName 필드 존재 여부 검증
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].baseRate").exists()) // 첫 번째 추천 상품의 baseRate 필드 존재 여부 검증
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].maxRate").exists()) // 첫 번째 추천 상품의 maxRate 필드 존재 여부 검증
                    .andExpect(jsonPath("$.recommendationsByPersona[0].products[0].totalScore").exists()); // 첫 번째 추천 상품의 totalScore 필드 존재 여부 검증

            log.info("--- [recommendSavings_success_withTopN] API 호출 및 응답 검증 완료 ---");
            log.info("  - 예상 HTTP 상태: 200 OK, 실제 HTTP 상태: {}", result.andReturn().getResponse().getStatus());
            log.info("  - 응답 본문: {}", result.andReturn().getResponse().getContentAsString());

            // Mock 서비스 호출 검증
            // mockSavingsRecommendationService의 recommendSavings 메서드가 정확히 한 번 호출되었는지,
            // 그리고 호출 시 userId와 topN 인자가 사용되었는지 검증합니다.
            verify(mockSavingsRecommendationService, times(1)).recommendSavings(eq(userId), eq(topN));
            log.info("--- [recommendSavings_success_withTopN] topN 파라미터 테스트 성공 ---");

            // 컨트롤러에서 발생한 예외가 있다면 로그 출력 (디버깅용)
            if (result.andReturn().getResolvedException() != null) {
                log.error("컨트롤러에서 발생한 예외: ", result.andReturn().getResolvedException());
            }
        } catch (Exception e) {
            // 테스트 실패 시 상세 에러 정보 로그 출력
            log.error("--- [recommendSavings_success_withTopN] 테스트 실패 - 상세 에러 정보: ---", e);
            throw e; // 예외를 다시 던져 테스트 실패를 알립니다.
        } finally {
            // 각 테스트 메서드 실행 후 SecurityContext를 초기화하여 테스트 간 독립성을 유지합니다.
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