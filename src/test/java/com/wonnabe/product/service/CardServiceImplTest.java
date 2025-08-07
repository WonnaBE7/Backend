package com.wonnabe.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.dto.CardProductDetailResponseDTO;
import com.wonnabe.product.dto.CardRecommendationResponseDTO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.mapper.CardMapper;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@Log4j2
@ActiveProfiles("test")
class CardServiceImplTest {

    @Autowired
    @Qualifier("cardServiceImpl")
    private CardService cardService;

    @Autowired
    private CardMapper cardMapper;

    // given
    private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

    // 유저 정보 가져와서 출력하여 테스트
    @Test
    void findUserCardDetail() {
        UserCardDetailDTO userCardDetailDTO = cardService.findUserCardDetail(2001, userId);
        assertNotNull(userCardDetailDTO);
        System.out.println("userCardDetailDTO = " + userCardDetailDTO);
    }

    @Test
    @Transactional
    void applyUserCard() throws Exception {
        String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";
        CardApplyRequestDTO cardApplyRequest = CardApplyRequestDTO
                .builder()
                .cardType("check")
                .productType("card")
                .cardId("2005")
                .linkedAccount("222-2222-2222")
                .build();

        cardService.applyUserCard(cardApplyRequest, userId);

        UserCardVO newCard = cardMapper.findUserCardByproductId(Long.parseLong(cardApplyRequest.getCardId()), userId);
        System.out.println("newCard = " + newCard);
        assertNotNull(newCard);
        String myCardIds = cardMapper.getMyCardIdsJson(userId);
        System.out.println("myCardIds = " + myCardIds);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Long> cardIds = objectMapper.readValue(myCardIds, new TypeReference<List<Long>>() {});
        assertTrue(cardIds.contains(newCard.getId()));

    }


    @Test
    @DisplayName("[성공] 사용자 워너비에 따른 카드 상품 추천")
    void recommendCards() {
        CardRecommendationResponseDTO recommendCards = cardService.recommendCards(userId, 5);
        assertNotNull(recommendCards);
        assertNotNull(recommendCards.getRecommendationsByPersona());
        assertNotNull(recommendCards.getRecommendationsByPersona().get(0).getProducts());
        assertTrue(recommendCards.getRecommendationsByPersona().size() <= 5);

        log.info("recommendCards = " + recommendCards);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // 줄바꿈 및 들여쓰기 설정

        try {
            String json = mapper.writeValueAsString(recommendCards);
            log.info("\n{}", json);
        } catch (Exception e) {
            log.error("recommendCards JSON 변환 실패", e);
        }
    }

    @Test
    @DisplayName("[성공] 추천 카드 정보 상세 조회")
    void getRecommendCardsDetail() {
        CardProductDetailResponseDTO cardDetail = cardService.findProductDetail(2300, userId);
        assertNotNull(cardDetail);
        assertNotNull(cardDetail.getCardInfo());
        assertNotNull(cardDetail.getNote());
		assertEquals(2300, cardDetail.getCardInfo().getCardId());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // 줄바꿈 및 들여쓰기 설정
        log.info("cardDetail = " + cardDetail);
        try {
            String json = mapper.writeValueAsString(cardDetail);
            log.info("\n{}", json);
        } catch (Exception e) {
            log.error("recommendCards JSON 변환 실패", e);
        }
    }

}