package com.wonnabe.product.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.service.CardService;

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
	RootConfig.class,
	ServletConfig.class
})
@Log4j2
class CardRecommendControllerTest {

	private MockMvc mockMvc; // 컨트롤러 테스트용 가짜 브라우저인 mockmvc

	private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";
	private final String wrongUserId = "1469a2a3-213d-427e-b29f-f79d58f51192";
	private final String noWonnabeUser = "i9012345-6789-0123-abcd-st78uv90wx12";

	@Autowired
	private CardService cardService; // 카드 서비스 자동 주입

	@Autowired
	private WebApplicationContext ctx; // 스프링 컨텍스트를 주입

	@BeforeEach
	public void setup() throws Exception {
		// Mockito 초기화
		MockitoAnnotations.openMocks(this);

		this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.build();
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("[성공] 카드 추천 상품 조회")
	void recommendCards() throws Exception {
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

		// 목 객체로 api 호출 후 응답 결과가 200인지 확인함
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations/cards"))
			.andExpect(status().isOk())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);

		// 내부 "code" 값이 200인지 검증
		Map<String, Object> responseMap = mapper.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(200, code, "응답 JSON의 code 필드는 200이어야 합니다.");
	}

	@Test
	@DisplayName("[실패] 등록되지 않은 사용자")
	void recommendCardsNonExistsUser() throws Exception {
		// 가짜 유저 데이터 생성
		UserVO userVO = new UserVO();
		userVO.setUserId(wrongUserId);
		userVO.setEmail("test@example.com");
		userVO.setPasswordHash("dummy-password");
		userVO.setName("테스트 유저");

		// SecurityContext에 사용자 인증 정보를 저장함
		CustomUser customUser = new CustomUser(userVO);
		Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		// 목 객체로 api 호출 후 응답 결과가 400대인지 확인함
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations/cards"))
			.andExpect(status().is4xxClientError())
			.andReturn();


		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();

		// 내부 "code" 값이 404인지 검증
		Map<String, Object> responseMap = mapper.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(404, code, "응답 JSON의 code 필드는 404이어야 합니다.");
	}

	@Test
	@DisplayName("[실패] 워너비를 선택하지 않은 사용자")
	void recommendCardsNonWonnabeUser() throws Exception {
		// 가짜 유저 데이터 생성
		UserVO userVO = new UserVO();
		userVO.setUserId(noWonnabeUser);
		userVO.setEmail("test@example.com");
		userVO.setPasswordHash("dummy-password");
		userVO.setName("테스트 유저");

		// SecurityContext에 사용자 인증 정보를 저장함
		CustomUser customUser = new CustomUser(userVO);
		Authentication auth = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		// 목 객체로 api 호출 후 응답 결과가 400대인지 확인함
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/recommendations/cards"))
			.andExpect(status().is4xxClientError())
			.andReturn();

		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();

		// 내부 "code" 값이 404인지 검증
		Map<String, Object> responseMap = mapper.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(404, code, "응답 JSON의 code 필드는 404이어야 합니다.");
	}



}