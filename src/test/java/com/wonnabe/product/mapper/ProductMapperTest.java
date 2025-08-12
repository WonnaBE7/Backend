package com.wonnabe.product.mapper;

import static com.wonnabe.product.dto.ProductResponseDTO.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
	RootConfig.class, RedisConfig.class
})
class ProductMapperTest {

	@Autowired
	private ProductMapper mapper;

	private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

	@Test
	@DisplayName("[성공] 카드 정보 요약 조회")
	void findCardSummaryByUserId() {
		List<Card> cards = mapper.findCardSummaryByUserId(userId);
		assertNotNull(cards);
		log.info(cards.toString());
	}

	@Test
	@DisplayName("[성공] 보험 정보 조회")
	void findInsuranceSummaryByUserId() {
		List<Insurance> insurances = mapper.findInsuranceSummaryByUserId(userId);
		assertNotNull(insurances);
		log.info(insurances.toString());
	}

	@Test
	@DisplayName("[성공] 예적금 요약 정보 조회")
	void findDepositSummaryByUserId() {
		List<Savings> deposits = mapper.findDepositSummaryByUserId(userId);
		assertNotNull(deposits);
		log.info(deposits.toString());
	}
}