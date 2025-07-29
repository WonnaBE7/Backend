package com.wonnabe.product.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/products")
@RequiredArgsConstructor
@Log4j2
public class UserProductController {

    private final CardService cardService;

    @GetMapping("{cardId}")
    public ResponseEntity<UserCardDetailDTO> getUserCardDetail(
            @PathVariable("cardId") int cardId, // url 경로를 파라미터로 매핑할때
            @AuthenticationPrincipal CustomUser customuser // 인증된 사용자 정보
    ) {
        // 아이디를 가져옴
        String userId = customuser.getUser().getUserId();

        // 서비스에서 만든 함수로 dto를 가져옴
        UserCardDetailDTO userCardDetailDTO = cardService.findUserCardDetail(cardId, userId);
        // 반환
        return ResponseEntity.ok(userCardDetailDTO);
    }
}
