package com.wonnabe.product.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.dto.WishListRequestDTO;
import com.wonnabe.product.dto.WishProductResponseDTO;
import com.wonnabe.product.mapper.CardMapper;
import com.wonnabe.product.mapper.WishListMapper;

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@Log4j2
class WishListServiceImplTest {

	private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

	@Autowired
	@Qualifier("wishListServiceImpl")
	private WishListService wishListService;

	@Autowired
	private WishListMapper mapper;

	@Test
	@DisplayName("[성공] 사용자 아이디로 관심 상품 조회")
	void findWishProductByUserId() {
		WishProductResponseDTO WishProduct = wishListService.findWishProductByUserId(userId);
		assertNotNull(WishProduct);

		log.info("관심 상품 = " + WishProduct);

	}

	@Test
	@Transactional
	@DisplayName("[성공] 사용자 정보 업데이트 - 추가")
	void addWishProduct() {
		WishListRequestDTO userRequest = WishListRequestDTO.builder()
			.action("add")
			.productId("2800")
			.productType("card")
			.build();

		wishListService.updateWishList(userId, userRequest);

		String wishListId = mapper.findWishListByUserId(userId);

		assertNotNull(wishListId);

		assertTrue(wishListId.contains("\"" + "2800" + "\""));

	}

	@Test
	@Transactional
	@DisplayName("[성공] 사용자 정보 업데이트 - 추가 후 삭제")
	void deleteWishProduct() {
		WishListRequestDTO userRequest = WishListRequestDTO.builder()
			.action("add")
			.productId("2800")
			.productType("card")
			.build();

		wishListService.updateWishList(userId, userRequest);

		String wishListId = mapper.findWishListByUserId(userId);

		assertNotNull(wishListId);

		assertTrue(wishListId.contains("\"" + "2800" + "\""));

		WishListRequestDTO userRequest2 = WishListRequestDTO.builder()
			.action("remove")
			.productId("2800")
			.productType("card")
			.build();

		String deleteWishListId = mapper.findWishListByUserId(userId);

		assertNotNull(deleteWishListId);

		assertTrue(deleteWishListId.contains("\"" + "2800" + "\""));

	}
}