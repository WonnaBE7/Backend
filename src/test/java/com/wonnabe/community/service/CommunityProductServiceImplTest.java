package com.wonnabe.community.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.community.dto.ProductDTO;
import com.wonnabe.product.mapper.CardMapper;
import com.wonnabe.product.service.CardService;

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@Log4j2
class CommunityProductServiceImplTest {

	@Autowired
	@Qualifier("communityProductServiceImpl")
	private CommunityProductService service;

	@Autowired
	ObjectMapper om;

	@Test
	@DisplayName("[성공] 게시판별 인기 상품 조회 성공")
	void findTop3ProductsByCommunityId() {
		// 인기 상품 조회
		List<ProductDTO> products = service.findTop3ProductsByCommunityId(1);
		// 상품의 없는 지 확인
		assertNotNull(products);
		// 상품의 수가 3개인지 확인
		assertEquals(3, products.size());

		// 출력
		log.info("상품의 수: " + products.size());
		log.info("상품: " + products);
	}
}