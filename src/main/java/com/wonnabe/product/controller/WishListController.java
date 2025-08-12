package com.wonnabe.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.WishListRequestDTO;
import com.wonnabe.product.dto.WishProductResponseDTO;
import com.wonnabe.product.service.CardService;
import com.wonnabe.product.service.WishListService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/wishlist")
public class WishListController {

	private final WishListService service;

	public WishListController(@Qualifier("wishListServiceImpl") WishListService service) {
		this.service = service;
	}

	/**
	 * 사용자 관심 상품 목록 조회
	 * @param user 인증된 사용자
	 * @return 관심 상품 목록
	 */
	@GetMapping("")
	ResponseEntity<Object> findUserWishList(@AuthenticationPrincipal CustomUser user) {
		String userId = user.getUser().getUserId();

		WishProductResponseDTO wishProduct = service.findWishProductByUserId(userId);

		return JsonResponse.ok("관심상품 목록 조회 성공", wishProduct);
	}

	/**
	 * 사용자 관심 상품 추가 혹은 제거
	 * @param user 인증된 사용자
	 * @param userRequest 사용자 요청
	 * @return 성공 메시지
	 */
	@PostMapping("")
	ResponseEntity<Object> updateWishList(@AuthenticationPrincipal CustomUser user, @RequestBody  WishListRequestDTO userRequest) {
		String userId = user.getUser().getUserId();

		String action = userRequest.getAction();
		String productType = userRequest.getProductType();
		String productId = userRequest.getProductId();

		// action 검증
		if (!"add".equals(action) && !"remove".equals(action)) {
			throw new IllegalArgumentException("action은 'add' 또는 'remove'만 가능합니다.");
		}

		// productType 검증
		List<String> validTypes = List.of("savings", "card", "insurance");
		if (!validTypes.contains(productType)) {
			throw new IllegalArgumentException("productType은 'savings', 'card', 'insurance' 중 하나여야 합니다.");
		}

		// productId 검증
		if (productId == null || productId.trim().isEmpty()) {
			throw new IllegalArgumentException("productId는 필수입니다.");
		}

		service.updateWishList(userId, userRequest);

		if (action.equals("add")) {
			return JsonResponse.ok("관심상품이 추가되었습니다.");
		}
		return JsonResponse.ok("관심상품이 제거되었습니다.");
	}

}
