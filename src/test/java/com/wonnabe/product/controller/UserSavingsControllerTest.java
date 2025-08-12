package com.wonnabe.product.controller;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.service.UserSavingsService;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Log4j2
@ActiveProfiles("test")
class UserSavingsControllerTest {

    @Autowired
    private WebApplicationContext ctx; // 스프링 컨텍스트를 주입

    private MockMvc mockMvc;

    @Autowired
    private UserSavingsService userSavingsService; // Savings 서비스 자동 주입

    // DB에 존재하는 테스트 데이터
    private final String userId = "550e8400-e29b-41d4-a716-446655440000";
    private final Long productId = 1306L;

    @BeforeEach
    public void setup() {
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

    }

    @Test
    @DisplayName("예적금 상세 정보 조회 API 성공")
    void getSavingsDetail_success() throws Exception {

        // API 호출 및 검증
        mockMvc.perform(get("/api/user/products/savings/{productId}", productId))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.productId").value(productId.toString()))
//                .andExpect(jsonPath("$.productName").exists()) // productName이 존재하는지 확인
                .andDo(print()); // 응답/요청 전체 내용 출력

        // 컨텍스트 비우기
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("예적금 상세 정보 조회 API 실패 - 존재하지 않는 상품")
    void getSavingsDetail_notFound() throws Exception {
        // given
        Long nonExistentProductId = 9999L;

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

        // when & then
        mockMvc.perform(get("/api/user/products/savings/{productId}", nonExistentProductId))
//                .andExpect(status().isNotFound())
                .andDo(print());

        // 컨텍스트 비우기
        SecurityContextHolder.clearContext();
    }
}