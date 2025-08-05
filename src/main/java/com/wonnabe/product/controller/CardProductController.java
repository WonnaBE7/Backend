package com.wonnabe.product.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.CardProductDetailResponseDTO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.service.CardService;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/products/cards")
public class CardProductController {
	private final CardService cardService;

	public CardProductController(@Qualifier("cardServiceImpl") CardService cardService) {
		this.cardService = cardService;
	}

	/**
	 * 카드 상품 상세 정보 조회
	 * @param cardId 카드 상품 아이디
	 * @param customuser 사용자 아이디
	 * @return 카드 상품 상세 정보
	 */
	@GetMapping("/{cardId}")
	public ResponseEntity<Object> getUserCardDetail(
		@PathVariable("cardId") int cardId, // url 경로를 파라미터로 매핑할때
		@AuthenticationPrincipal CustomUser customuser // 인증된 사용자 정보
	) {
		// 아이디를 가져옴
		String userId = customuser.getUser().getUserId();

		// 서비스에서 만든 함수로 dto를 가져옴
		CardProductDetailResponseDTO cardDetail = cardService.findProductDetail(cardId, userId);
		// 반환
		return JsonResponse.ok("카드 상품 상세 조회 성공", cardDetail);
	}
}
