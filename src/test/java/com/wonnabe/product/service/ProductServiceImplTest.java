package com.wonnabe.product.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.dto.ProductResponseDTO;

import lombok.extern.log4j.Log4j2;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@Log4j2
class ProductServiceImplTest {

	private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService service;

	@Test
	@DisplayName("[성공] 사용자 상품 요약 조회")
	void findUserProductSummary() {
		ProductResponseDTO products = service.findUserProductSummary(userId);

		assertNotNull(products);

		log.info(products);
	}
}