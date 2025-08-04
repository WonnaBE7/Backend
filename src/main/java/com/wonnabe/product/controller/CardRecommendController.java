package com.wonnabe.product.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.CardRecommendationResponseDTO;
import com.wonnabe.product.dto.SavingsRecommendationResponseDTO;
import com.wonnabe.product.service.CardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/recommendations/cards")
@Log4j2
public class CardRecommendController {

	private final CardService cardService;

	public CardRecommendController(@Qualifier("cardServiceImpl") CardService cardService) {
		this.cardService = cardService;
	}

	/**
	 * 카드 상품 추천
	 * @param topN 몇 개의 상품을 볼 것 인지
	 * @param customUser 사용자 정보
	 * @return 페르소나별 추천 상품
	 */
	@GetMapping
	public ResponseEntity<Object> recommendCards(
		@RequestParam(defaultValue = "5") int topN,
		@AuthenticationPrincipal CustomUser customUser) {

		String userId = customUser.getUser().getUserId();

		CardRecommendationResponseDTO recommendations = cardService.recommendCards(userId, topN);

		return JsonResponse.ok("카드 추천 상품 조회 성공", recommendations);
	}

}
