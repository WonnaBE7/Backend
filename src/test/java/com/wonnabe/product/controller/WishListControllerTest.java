package com.wonnabe.product.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.config.ServletConfig;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.product.dto.WishListRequestDTO;

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
	RootConfig.class,
	ServletConfig.class
})
@Log4j2
class WishListControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext ctx;

	@Autowired
	private ObjectMapper om;

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
	@DisplayName("[성공] 사용자 관심 상품 정보 조회")
	void findUserWishList() throws Exception {
		// 목 객체로 api 호출 후 응답 결과가 200인지 확인함
		MvcResult result = mockMvc.perform(get("/api/user/wishlist"))
			.andExpect(status().isOk())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectWriter prettyPrinter = om.writerWithDefaultPrettyPrinter();

		Object jsonObject = om.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);

		// 내부 "code" 값이 200인지 검증
		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(200, code, "응답 JSON의 code 필드는 200이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[성공] 사용자 관심 상품 추가")
	void addUserWishListWithException() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("add")
			.productType("card")
			.productId("2800")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(200, code, "응답 JSON의 code 필드는 200이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[성공] 사용자 관심 상품 제거")
	void removeUserWishListWithException() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("remove")
			.productType("card")
			.productId("1110")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(200, code, "응답 JSON의 code 필드는 200이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[실패] 잘못된 액션으로 사용자 관심 상품 추가 실패")
	void updateUserWishListWithExceptionFailIllegalAction() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("ady")
			.productType("card")
			.productId("2800")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().is4xxClientError())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(400, code, "응답 JSON의 code 필드는 400이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[실패] 잘못된 상품 정보로 사용자 관심 상품 추가 실패")
	void updateUserWishListWithExceptionFailIllegalProductType() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("add")
			.productType("car")
			.productId("2800")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().is4xxClientError())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(400, code, "응답 JSON의 code 필드는 400이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[실패] 상품 아이디 없이 사용자 관심 상품 추가 실패")
	void updateUserWishListWithExceptionFailWithNonProductId() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("add")
			.productType("card")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().is4xxClientError())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(400, code, "응답 JSON의 code 필드는 400이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[실패] 존재하는 상품 아이디로 사용자 관심 상품 추가 실패")
	void updateUserWishListWithExceptionFailDuplicateId() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("add")
			.productType("card")
			.productId("1110")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().is4xxClientError())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(400, code, "응답 JSON의 code 필드는 400이어야 합니다.");
	}

	@Test
	@Transactional
	@DisplayName("[실패] 유저가 보유하지 않은 상품 아이디로 사용자 관심 상품 제거 실패")
	void updateUserWishListWithExceptionFailNonExistingProductId() throws Exception {
		WishListRequestDTO dto = WishListRequestDTO
			.builder()
			.action("remove")
			.productType("card")
			.productId("1300")
			.build();


		log.info("요청 JSON = {}", om.writeValueAsString(dto));

		String jsonRequest = om.writeValueAsString(dto);

		MvcResult result = mockMvc.perform(post("/api/user/wishlist")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().is4xxClientError())
			.andReturn();

		// json 형식으로 가져온 데이터 출력
		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter prettyPrinter = mapper.writerWithDefaultPrettyPrinter();

		Object jsonObject = mapper.readValue(json, Object.class);
		String prettyJson = prettyPrinter.writeValueAsString(jsonObject);

		log.info("응답 JSON (pretty):\n{}", prettyJson);


		Map<String, Object> responseMap = om.readValue(json, new TypeReference<>() {});
		Integer code = (Integer) responseMap.get("code");
		assertEquals(404, code, "응답 JSON의 code 필드는 404이어야 합니다.");
	}


}