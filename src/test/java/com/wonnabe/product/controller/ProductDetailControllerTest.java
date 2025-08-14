package com.wonnabe.product.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class
})
class ProductDetailControllerTest {

    private MockMvc mockMvc;

    private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    private void setupAuthentication(String userId) {
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");  // 이메일 설정 추가
        userVO.setPasswordHash("testpassword");  // 비밀번호 해시 설정 추가
        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("[성공] 예적금 상세 조회 (로그인 사용자)")
    void getSavingProductDetail_loggedIn_success() throws Exception {
        // given
        String productId = "1310";
        setupAuthentication(userId);

        // when & then
        mockMvc.perform(get("/api/products/savings")
                        .param("productId", productId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 예적금 상세 조회 (로그인 사용자, wannabeId 파라미터 포함)")
    void getSavingProductDetail_withWannabeId_success() throws Exception {
        // given
        String productId = "1310";
        String wannabeId = "3";
        setupAuthentication(userId);

        // when & then
        mockMvc.perform(get("/api/products/savings")
                        .param("productId", productId)
                        .param("wannabeId", wannabeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 예적금 상세 조회 (비로그인 사용자)")
    void getSavingProductDetail_guest_success() throws Exception {
        // given
        String productId = "1310";

        // when & then
        mockMvc.perform(get("/api/products/savings")
                        .param("productId", productId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 보험 상세 조회 (로그인 사용자)")
    void getInsuranceProductDetail_loggedIn_success() throws Exception {
        // given
        String productId = "3001";
        setupAuthentication(userId);

        // when & then
        mockMvc.perform(get("/api/products/insurances")
                        .param("productId", productId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 보험 상세 조회 (로그인 사용자, wannabeId 파라미터 포함)")
    void getInsuranceProductDetail_withWannabeId_success() throws Exception {
        // given
        String productId = "3001";
        String wannabeId = "2";
        setupAuthentication(userId);

        // when & then
        mockMvc.perform(get("/api/products/insurances")
                        .param("productId", productId)
                        .param("wannabeId", wannabeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 보험 상세 조회 (비로그인 사용자)")
    void getInsuranceProductDetail_guest_success() throws Exception {
        // given
        String productId = "3001";

        // when & then
        mockMvc.perform(get("/api/products/insurances")
                        .param("productId", productId))
                .andExpect(status().isOk());
    }
}
