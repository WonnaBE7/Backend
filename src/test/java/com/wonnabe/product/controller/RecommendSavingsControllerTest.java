package com.wonnabe.product.controller;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Log4j2
@ActiveProfiles("test") // "test" 프로파일을 사용하여 테스트용 DB에 연결합니다.
@Transactional // 각 테스트 후 DB 변경사항을 롤백하여 테스트 독립성을 보장합니다.
class RecommendSavingsControllerTest {

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    // 실제DB 페르소나 ID
    private final String userId = "a1b2c3d4-e5f6-7890-ab12-cd34ef56gh78";

    @BeforeEach
    public void setup() {
        // Mockito 초기화 코드를 제거
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        // 테스트용 사용자 인증 정보 설정
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("dummy-password");
        userVO.setName("테스트 유저");

        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 SecurityContext를 초기화하여 다른 테스트에 영향을 주지 않도록 합니다.
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("예적금 추천 API 통합 테스트 - 실제 DB 연동")
    void recommendSavings_integrationTest() throws Exception {
        // given
        // 1. `user_info` 테이블에 `userId`가 존재하고, 페르소나 ID가 할당되어 있어야 함
        // 2. `savings_product` 테이블에 상품 데이터가 존재하고, 5가지 개별 점수 컬럼에 0이 아닌 값이 있어야 함
        int topN = 5;

        // when & then - API 호출 및 응답 구조 검증
        mockMvc.perform(get("/api/recommendations/savings")
                        .param("topN", String.valueOf(topN)))
                .andDo(print()) // 요청/응답 전체 내용을 콘솔에 출력합니다.
                .andExpect(status().isOk()) // HTTP 200 OK 상태를 확인합니다.
                .andExpect(jsonPath("$.message").value("성공적으로 추천 예적금상품을 반환하였습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.recommendationsByPersona").exists()) // 페르소나별 추천 결과가 존재하는지 확인
                .andExpect(jsonPath("$.data.recommendationsByPersona").isArray()) // 페르소나별 추천 결과가 배열인지 확인
                .andExpect(jsonPath("$.data.recommendationsByPersona[0].products").isArray()) // 첫 번째 페르소나의 추천 상품 목록이 배열인지 확인
                .andExpect(jsonPath("$.data.recommendationsByPersona[0].products", hasSize(topN))) // 추천된 상품의 개수가 topN과 일치하는지 확인
                .andExpect(jsonPath("$.data.recommendationsByPersona[0].products[0].score").isNumber()) // 점수가 숫자인지 확인
                .andExpect(jsonPath("$.data.recommendationsByPersona[0].products[0].score", greaterThan(0.0))); // 점수가 0보다 큰 값인지 확인
    }
}