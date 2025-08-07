package com.wonnabe.product.service;

import static com.wonnabe.product.dto.ProductResponseDTO.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wonnabe.product.dto.ProductResponseDTO;
import com.wonnabe.product.mapper.ProductMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service("productServiceImpl")
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductMapper mapper;

	@Override
	public ProductResponseDTO findUserProductSummary(String userId) {
		// 사용자 카드 요약 정보 조회
		List<Card> cards = mapper.findCardSummaryByUserId(userId);
		// 사용자 예적금 요약 정보 조회
		List<Deposit> deposits = mapper.findDepositSummaryByUserId(userId);
		// 사용자 보험 요약 정보 조회
		List<Insurance> insurances = mapper.findInsuranceSummaryByUserId(userId);

		// 카드 정보 가공
		CardWithCount cardWithCount = CardWithCount.builder()
			.count(cards.size())
			.products(cards)
			.build();

		// 예적금 정보 가공
		DepositsWithCount depositsWithCount = DepositsWithCount.builder()
			.count(deposits.size())
			.products(deposits)
			.build();

		// 보험 정보 가공
		InsuranceWithCount insuranceWithCount = InsuranceWithCount.builder()
			.count(insurances.size())
			.products(insurances)
			.build();

		ProductResponseDTO products =  ProductResponseDTO.builder()
			.cards(cardWithCount)
			.deposits(depositsWithCount)
			.insurances(insuranceWithCount)
			.build();

		// 설명 단순화
		products.makeSimpleDescription();

		return products;
	}
}
