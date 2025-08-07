package com.wonnabe.community.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.community.dto.ProductDTO;

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j2
class CommunityProductMapperTest {

	@Autowired
	private CommunityProductMapper mapper;

	@Test
	@DisplayName("[성공] 인기 게시글 Top3 조회 성공")
	void findTop3ProductsByCommunityId() {
		// 인기 상품 조회
		List<ProductDTO> products = mapper.findTop3ProductsByCommunityId(1);
		// 인기 상품의 값이 있는 지 확인
		assertNotNull(products);
		// 인기 상품의 갯수가 3개 인지 확인
		assertEquals(3, products.size());
		// 상품 결과 출력
		log.info(products);
	}
}