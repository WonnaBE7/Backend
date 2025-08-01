package com.wonnabe.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.mapper.CardMapper;
import lombok.extern.log4j.Log4j2;
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

    // 유저 정보 가져와서 출력하여 테스트
    @Test
    void findUserCardDetail() {
        UserCardDetailDTO userCardDetailDTO = cardService.findUserCardDetail(2001, "1469a2a3-213d-427e-b29f-f79d58f51190");
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
                .cardId("2003")
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
}