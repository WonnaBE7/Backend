package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.service.CardService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/card")
@Log4j2
public class CardApplyController {
    private final CardService cardService;

    public CardApplyController(@Qualifier("cardServiceImpl") CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * 사용자 카드 신청 API
     * @param cardApplyRequestDTO 신청하는 카드와 연결 계좌에 대한 정보
     * @param customuser 카드를 신청할 사용자 정보
     * @return 반환하지 않음
     */
    @PostMapping("/apply")
    public ResponseEntity<Object> applyCard(
            @RequestBody CardApplyRequestDTO cardApplyRequestDTO,
            @AuthenticationPrincipal CustomUser customuser
    ) {
        if (cardApplyRequestDTO == null) {
            throw new IllegalArgumentException("잘못된 입력 양식입니다.");
        }

        String userId = customuser.getUser().getUserId();

        cardService.applyUserCard(cardApplyRequestDTO, userId);

        return JsonResponse.ok("카드 신청을 완료되었습니다");
    }
}
