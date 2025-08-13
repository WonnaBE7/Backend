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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Log4j2
@ActiveProfiles("test")
@Transactional // 각 테스트 후 롤백하여 DB 상태를 유지
class UserSavingsControllerTest {

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    // DB에 존재하는 테스트 데이터
    private final String userId = "550e8400-e29b-41d4-a716-446655440000";
    private final Long existingProductId = 1306L;
    private final Long nonExistentProductId = 9999L;

    @BeforeEach
    public void setup() {
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
        // 테스트 후 SecurityContext 클리어
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("예적금 상세 정보 조회 성공")
    void getSavingsDetail_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/user/products/savings/{productId}", existingProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 예적금 정보 조회 성공"))
                .andExpect(jsonPath("$.data.productId").value(existingProductId))
                .andExpect(jsonPath("$.data.productName").exists())
                .andExpect(jsonPath("$.data.bankName").exists())
                .andExpect(jsonPath("$.data.baseRate").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("예적금 상세 정보 조회 시, 존재하지 않는 상품 ID는 200 OK와 null 데이터를 반환")
    void getSavingsDetail_returnsOkWithNullData_forNonExistentProduct() throws Exception {
        // when & then
        mockMvc.perform(get("/api/user/products/savings/{productId}", nonExistentProductId))
                .andExpect(status().isOk()) // 서비스가 null을 반환하므로 200 OK가 됨
                .andExpect(jsonPath("$.message").value("내 예적금 정보 조회 성공"))
                .andExpect(jsonPath("$.data").isEmpty()) // 데이터 필드가 비어있는지(null) 확인
                .andDo(print());
    }

}