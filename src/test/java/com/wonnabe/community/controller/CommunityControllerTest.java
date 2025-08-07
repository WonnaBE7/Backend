package com.wonnabe.community.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
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

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
	RootConfig.class,
	ServletConfig.class
})
@Log4j2
class CommunityControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext ctx;

	@BeforeEach
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.build();
	}

	@Test
	@DisplayName("[성공] 게시판별 인기 상품 조회")
	void findTop3ProductsByCommunityId() throws Exception {
		// 200인지 확인
		MvcResult result = mockMvc.perform(get("/api/community/popular/1"))
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
		Map<String, Object> response = mapper.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) response.get("code");
		assertEquals(200, code, "응답 JSON의 code 필드는 200이어야 합니다.");
	}

	@Test
	@DisplayName("[실패] 잘못된 게시판 아이디로 게시판별 인기 상품 조회")
	void findTop3ProductsByWrongCommunityId() throws Exception {
		// 400대인지 확인
		MvcResult result = mockMvc.perform(get("/api/community/popular/13"))
			.andExpect(status().is4xxClientError())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);

		// 내부 "code" 값이 400인지 검증
		Map<String, Object> response = mapper.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) response.get("code");
		assertEquals(400, code, "응답 JSON의 code 필드는 400이어야 합니다.");
	}
}