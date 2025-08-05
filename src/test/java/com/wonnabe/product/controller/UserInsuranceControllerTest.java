package com.wonnabe.product.controller;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.service.UserInsuranceService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class
})
@Log4j2
@ActiveProfiles("test")
class UserInsuranceControllerTest {

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    @Autowired
    private UserInsuranceService userInsuranceService; //  서비스 자동 주입

    // DB존재 데이터
    private final String userId = "b2c3d4e5-f678-9012-abcd-ef12gh34ij56";
    private final Long productId = 3002L;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
        // 가짜 유저 데이터 생성
        UserVO userVO = new UserVO();
        userVO.setUserId(userId);
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("dummy-password"); // User 객체 생성 시 필수 값이므로 임시 비밀번호를 설정합니다.
        userVO.setName("테스트유저");

        // SecurityContext에 사용자 인증 정보를 저장함
        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

    }

    @Test
    @DisplayName("GET /api/insurances/{productId} - 보유 보험 상세 정보를 성공적으로 조회해야 한다.")
    void getMyInsuranceDetail_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/user/insurances/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                .andExpect(jsonPath("$.data.productName").exists())
                .andDo(print());

        // 컨텍스트 비우기
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/user/insurances/{productId} - 존재하지 않는 ID로 조회 시 404 Not Found를 반환해야 한다.")
    void getMyInsuranceDetail_notFound() throws Exception {
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
        mockMvc.perform(get("/api/user/insurances/{productId}", nonExistentProductId))
                .andExpect(status().isNotFound())
                .andDo(print());

        // 컨텍스트 비우기
        SecurityContextHolder.clearContext();
    }

}