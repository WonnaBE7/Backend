package com.wonnabe.product.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.wonnabe.common.config.RootConfig;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@Slf4j
class WishListMapperTest {

	@Autowired
	private WishListMapper wishListMapper;

	private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";
	private final String productId = "2800";
	@Test
	@DisplayName("[성공] 사용자 선호 상품 목록 조회")
	void findWishListByUserId() {
		String wishListId = wishListMapper.findWishListByUserId(userId);
		assertNotNull(wishListId);
		log.info(wishListId);
	}

	@Test
	@Transactional
	@DisplayName("[성공] 사용자 선호 상품 추가")
	void addWishList() {
		wishListMapper.addWishList(userId, productId);
		String wishListId = wishListMapper.findWishListByUserId(userId);
		assertTrue(wishListId.contains("\"" + productId + "\""));
	}

	@Test
	@Transactional
	@DisplayName("[성공] 사용자 선호 상품 추가 후 삭제")
	void deleteWishList() {
		wishListMapper.addWishList(userId, productId);
		String wishListId = wishListMapper.findWishListByUserId(userId);
		assertTrue(wishListId.contains("\"" + productId + "\""));
		log.info("선호 상품 목록: " + wishListId);
		wishListMapper.deleteWishList(userId, productId);
		String deleteWishListId = wishListMapper.findWishListByUserId(userId);
		assertFalse(deleteWishListId.contains("\"" + productId + "\""));
		log.info("선호 상품 목록: " + wishListId);
	}
}