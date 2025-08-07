package com.wonnabe.community.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.community.dto.ProductDTO;
import com.wonnabe.community.service.CommunityProductService;
import com.wonnabe.product.service.CardService;

@RestController
@RequestMapping("/api/community/popular")
public class CommunityController {

	private final CommunityProductService service;

	public CommunityController(@Qualifier("communityProductServiceImpl") CommunityProductService service) {
		this.service = service;
	}

	/**
	 * 게시판별 인기 상품 조회
	 * @param communityId 게시판 아이디
	 * @return 게시판별 인기 상품
	 */
	@GetMapping("/{communityId}")
	public ResponseEntity<Object> findTop3ProductsByCommunityId(@PathVariable int communityId) {
		if (communityId < 1 || communityId > 12) {
			throw new IllegalArgumentException("게시판 아이디는 1~12 사이여야 합니다.");
		}

		List<ProductDTO> products = service.findTop3ProductsByCommunityId(communityId);

		return JsonResponse.ok("성공적으로 인기 상품을 불러왔습니다.", products);
	}
}
