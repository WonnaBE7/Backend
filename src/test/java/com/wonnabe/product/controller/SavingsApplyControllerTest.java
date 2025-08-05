package com.wonnabe.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.dto.SavingsApplyRequestDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@WebAppConfiguration // WebApplicationContext를 사용하기 위한 어노테이션
@ContextConfiguration(classes = {
        RootConfig.class,
        ServletConfig.class
})
@Log4j2
class SavingsApplyControllerTest {

    @Autowired
    private WebApplicationContext ctx; // 스프링 컨텍스트를 주입

    private MockMvc mockMvc; // 컨트롤러 테스트를 위한 MockMvc 객체

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 직렬화를 위한 객체

    @BeforeEach
    public void setup() {
        // MockMvc 객체를 스프링 컨텍스트 기반으로 초기화
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 인코딩 필터 추가
                .build();

        // 테스트를 위한 가짜 사용자 정보 생성
        UserVO userVO = new UserVO();
        userVO.setUserId("1469a2a3-213d-427e-b29f-f79d58f51190"); // 실제 DB에 존재하는 테스트용 사용자 ID
        userVO.setEmail("test@example.com");
        userVO.setPasswordHash("dummy-password");
        userVO.setName("테스트유저");

        // Spring Security 컨텍스트에 인증 정보 설정
        CustomUser customUser = new CustomUser(userVO);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUser, null, customUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        // 테스트 후 Security 컨텍스트를 초기화하여 다른 테스트에 영향이 없도록 함
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional // 테스트 후 DB 변경사항을 롤백하여 테스트의 독립성을 보장
    @DisplayName("사용자 예적금 신청 성공 테스트")
    void applySavings_success() throws Exception {
        // given: 테스트를 위한 요청 데이터 준비
        SavingsApplyRequestDTO dto = SavingsApplyRequestDTO.builder()
                .productId(1310L) // 실제 DB에 존재하는 테스트용 예적금 상품 ID
                .principalAmount(1000000L)
                .monthlyPayment(100000L)
                .joinPeriod(12)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(dto);

        // when & then: API를 호출하고, 정상적으로 처리되는지(HTTP 200 OK) 확인
        mockMvc.perform(post("/api/users/savings/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("잘못된 예적금 ID로 신청 시 실패 테스트")
    void applySavings_fail_with_invalid_id() throws Exception {
        // given: 존재하지 않는 예적금 ID로 요청 데이터 준비
        SavingsApplyRequestDTO dto = SavingsApplyRequestDTO.builder()
                .productId(9999L) // 존재하지 않는 ID
                .principalAmount(1000000L)
                .monthlyPayment(100000L)
                .joinPeriod(12)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(dto);

        // when & then: API를 호출하고, 리소스를 찾을 수 없다는 의미의 HTTP 404 Not Found가 반환되는지 확인
        mockMvc.perform(post("/api/users/savings/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }
}
