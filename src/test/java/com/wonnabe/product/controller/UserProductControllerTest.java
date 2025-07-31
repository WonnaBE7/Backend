package com.wonnabe.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class
})
@Log4j2
@ActiveProfiles("test")
class UserProductControllerTest {

    private MockMvc mockMvc; // 컨트롤러 테스트용 가짜 브라우저인 mockmvc


    @Autowired
    private CardService cardService; // 카드 서비스 자동 주입

    @Autowired
    private WebApplicationContext ctx; // 스프링 컨텍스트를 주입

    @BeforeEach
    public void setup() {
        // 테스트 전 mockmvc 객체를 컨텍스트 기반으로 초기화함
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                // json 한글 인코딩 처리
                .addFilters(new CharacterEncodingFilter("UTF-8", true)).build();
    }

    @Test
    void getUserCardDetail() throws Exception {
        // 가짜 유저 데이터 생성
        UserVO userVO = new UserVO();
        userVO.setUserId("1469a2a3-213d-427e-b29f-f79d58f51190");
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("dummy-password");
        userVO.setName("테스트 유저");

        // SecurityContext에 사용자 인증 정보를 저장함
        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 목 객체로 api 호출 후 응답 결과가 200인지 확인함
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/user/products/3001"))
                .andExpect(status().isOk())
                .andReturn();

        // json 형식으로 가져온 데이터 출력
        String json = result.getResponse().getContentAsString();
        System.out.println("응답 JSON: " + json);

        // 컨텍스트 비우기
        SecurityContextHolder.clearContext();
    }
}