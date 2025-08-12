package com.wonnabe.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WishListMapper {
	/**
	 * 사용자 선호 상품 목록을 조회
	 * @param userId 사용자 아이디
	 * @return 사용자 선호 상품 목록
	 */
	String findWishListByUserId(String userId);

	/**
	 * 사용자 선호 상품 추가
	 * @param userId 사용자 아이디
	 * @param productId 상품 아이디
	 */
	void addWishList(@Param("userId") String userId,@Param("productId") String productId);

	/**
	 * 사용자 선호 상품 삭제
	 * @param userId 사용자 아이디
	 * @param productId 상품 아이디
	 */
	void deleteWishList(@Param("userId") String userId,@Param("productId") String productId);
}
