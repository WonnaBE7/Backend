package com.wonnabe.community.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.wonnabe.community.dto.ProductDTO;
import com.wonnabe.community.mapper.CommunityProductMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("communityProductServiceImpl")
@RequiredArgsConstructor
public class CommunityProductServiceImpl implements CommunityProductService {
	private final CommunityProductMapper mapper;

	@Override
	public List<ProductDTO> findTop3ProductsByCommunityId(int communityId) {
		// 인기 상품 조회
		List<ProductDTO> products = mapper.findTop3ProductsByCommunityId(communityId);

		// 조회한 상품이 없을 시 예외
		if (products == null || products.isEmpty()) {
			throw new NoSuchElementException("인기 상품이 존재하지 않습니다.");
		}

		// 상품의 설명 간단하게 만들기
		products.forEach(ProductDTO::makeSimpleDescription);

		return products;
	}
}
