package com.wonnabe.product.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.service.ProductDetailService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @ExtendWith(SpringExtension.class): JUnit 5에서 Spring 테스트 컨텍스트를 활성화합니다.
 * @WebAppConfiguration: 웹 애플리케이션의 ApplicationContext를 로드합니다. (통합 테스트용)
 * @ContextConfiguration: 로드할 Spring 설정 클래스들을 지정합니다.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class
})
@Log4j2
class ProductDetailControllerTest {

    // MockMvc는 서블릿 컨테이너를 모의(mock)하여 실제 네트워크 연결 없이 컨트롤러를 테스트할 수 있게 합니다.
    private MockMvc mockMvc;

    // 테스트에 사용할 가짜 사용자 ID
    private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190"; // 실제 DB에 존재하는 테스트용 사용자 UUID

    @Autowired
    private ProductDetailService productDetailService; // product 서비스 자동 주입

    // WebApplicationContext는 Spring의 전체 웹 애플리케이션 설정을 담음
    @Autowired
    private WebApplicationContext ctx;

    /**
     * @BeforeEach: 각 테스트 메서드 실행 전에 호출됩니다.
     * 여기서는 MockMvc 객체를 초기화하고, 한글 깨짐 방지를 위한 필터를 추가합니다.
     */
    @BeforeEach
    public void setup() throws Exception {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    /**
     * @AfterEach: 각 테스트 메서드 실행 후에 호출됩니다.
     * 테스트 간의 독립성을 보장하기 위해 SecurityContext를 초기화합니다.
     */
    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    /**
     * 테스트용 가짜 인증(Authentication) 객체를 생성하고 SecurityContextHolder에 설정하는 헬퍼 메서드입니다.
     * @param userId 인증할 사용자의 ID
     */
    private void setupAuthentication(String userId) {
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("password"); // 실제 비밀번호는 중요하지 않음

        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("[성공] 예적금 상품 상세 정보 조회")
    void getSavingProductDetail_success() throws Exception {
        // given: 테스트 사전 조건 설정
        String productId = "1310"; // DB에 존재하는 예적금 상품 ID
        setupAuthentication(userId); // "userId" 사용자로 로그인 상태를 시뮬레이션

        // when: MockMvc를 사용하여 API를 호출하고, andExpect로 결과를 기대하는 단계
        // perform()으로 GET 요청을 보내고, andExpect()로 HTTP 상태 코드가 200(OK)인지 확인합니다.
        MvcResult result = mockMvc.perform(get("/api/products/savings/{productId}", productId))
                .andExpect(status().isOk())
                .andReturn(); // 결과를 MvcResult 객체로 받음

        // then: 응답 결과를 검증하는 단계
        String jsonResponse = result.getResponse().getContentAsString();
        log.info("응답 JSON (raw):\n{}", jsonResponse);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = null;
        try {
            responseMap = mapper.readValue(jsonResponse, new TypeReference<>() {});
            Object jsonObject = mapper.readValue(jsonResponse, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            log.info("응답 JSON (pretty):\n{}", prettyJson);
        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage(), e);
            fail("JSON 응답 파싱 실패: " + e.getMessage());
        }

        // 응답의 최상위 "code" 필드가 200인지 확인하여 API 호출이 성공했는지 검증합니다.
        Integer code = (Integer) responseMap.get("code");
        assertEquals(200, code, "응답 JSON의 code 필드는 200이어야 합니다.");
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 예적금 상품 정보 조회")
    void getSavingProductDetail_notFound() throws Exception {
        // given
        String nonExistentProductId = "9999"; // DB에 존재하지 않는 상품 ID
        setupAuthentication(userId);

        // when & then
        // 존재하지 않는 상품을 조회하므로 4xx 클라이언트 에러(예: 404 Not Found)를 기대합니다.
        mockMvc.perform(get("/api/products/savings/{productId}", nonExistentProductId))
                .andExpect(status().isInternalServerError()); // 500 Internal Server Error를 기대
    }

    @Test
    @DisplayName("[성공] 보험 상품 상세 정보 조회")
    void getInsuranceProductDetail_success() throws Exception {
        // given
        String productId = "3001"; // DB에 존재하는 보험 상품 ID
        setupAuthentication(userId);

        // when
        MvcResult result = mockMvc.perform(get("/api/products/insurances/{productId}", productId))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String jsonResponse = result.getResponse().getContentAsString();
        log.info("보험 응답 JSON (raw):\n{}", jsonResponse);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = null;
        try {
            responseMap = mapper.readValue(jsonResponse, new TypeReference<>() {});
            Object jsonObject = mapper.readValue(jsonResponse, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            log.info("보험 응답 JSON (pretty):\n{}", prettyJson);
        } catch (Exception e) {
            log.error("보험 JSON 파싱 중 오류 발생: {}", e.getMessage(), e);
            fail("보험 JSON 응답 파싱 실패: " + e.getMessage());
        }

        Integer code = (Integer) responseMap.get("code");
        assertEquals(200, code, "보험 응답 JSON의 code 필드는 200이어야 합니다.");
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 보험 상품 정보 조회")
    void getInsuranceProductDetail_notFound() throws Exception {
        // given
        String nonExistentProductId = "9999"; // DB에 존재하지 않는 보험 상품 ID
        setupAuthentication(userId);

        // when & then
        mockMvc.perform(get("/api/products/insurances/{productId}", nonExistentProductId))
                .andExpect(status().isInternalServerError()); // 500 Internal Server Error를 기대
    }
}