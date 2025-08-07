package com.wonnabe.community.mapper;

import java.util.List;

import com.wonnabe.community.dto.ProductDTO;

public interface CommunityProductMapper {
	/**
	 * 게시판별 인기 상품 Top3 조회
	 * @param communityId 게시판 아이디
	 * @return 인기 상품 Top3
	 */
	List<ProductDTO> findTop3ProductsByCommunityId(int communityId);
}
