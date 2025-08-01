package com.wonnabe.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class
})
@Log4j2
class CardApplyControllerTest {

    private MockMvc mockMvc;


    @Autowired
    private WebApplicationContext ctx;



    @BeforeEach
    public void setup() {
        // 테스트 전 mockmvc 객체를 컨텍스트 기반으로 초기화함
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                // json 한글 인코딩 처리
                .addFilters(new CharacterEncodingFilter("UTF-8", true)).build();

        UserVO userVO = new UserVO();
        userVO.setUserId("1469a2a3-213d-427e-b29f-f79d58f51190");
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("dummy-password");
        userVO.setName("테스트 유저");

        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUser, null, customUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void applyCard() throws Exception {
        CardApplyRequestDTO dto = CardApplyRequestDTO.builder()
                .cardId("2003")
                .linkedAccount("222-2222-2222")
                .cardType("check")
                .productType("card")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/users/card/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isOk());
    }
}