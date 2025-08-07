package com.wonnabe.community.service;

import java.util.List;

import com.wonnabe.community.dto.ProductDTO;

public interface CommunityProductService {
	/**
	 * 게시판별 Top3 상품 조회
	 * @param communityId 게시판 아이디
	 * @return 인기 상품 Top3
	 */
	List<ProductDTO> findTop3ProductsByCommunityId(int communityId);
}
