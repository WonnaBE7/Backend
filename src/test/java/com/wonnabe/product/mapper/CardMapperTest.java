package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.MonthlyConsumptionDTO;
import com.wonnabe.product.dto.UserCardDTO;
import com.wonnabe.product.dto.UserInfoForCardDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class, RedisConfig.class
})
@ActiveProfiles("test")
public class CardMapperTest {
    @Autowired
    private CardMapper cardMapper;
    // 카드 상품 정보 가져오는 거 테스트

    // given
    private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

    @Test
    void testFindByProductId() {
        CardProductVO cardProductVO = cardMapper.findById(2001);
        assertNotNull(cardProductVO);
        assertEquals(2001, cardProductVO.getProductId());
        System.out.println(cardProductVO);
    }

    // 사용자 카드 정보 가져오는거 테스트
    @Test
    void testFindUserCardByproductId() {
        UserCardVO userCardVO = cardMapper.findUserCardByproductId(2001, userId);
        assertNotNull(userCardVO);
        assertEquals(2001, userCardVO.getProductId());
        assertEquals(userId, userCardVO.getUserId());
        log.info("userCardVO = " + userCardVO);
    }


    // 사용자 카드와 거래내역 join한거 테스트
    @Test
    void testFindUserCardDetailByProductId() {
        UserCardDTO userCardDTO = cardMapper.findUserCardDetailById(2001, userId);
        assertNotNull(userCardDTO);
        assertEquals(2001, userCardDTO.getProductId());
        for (MonthlyConsumptionDTO m : userCardDTO.getConsumptions()) {
            System.out.println("month: " + m);
        }
    }

    @Test
    @Transactional
    void testApplyCard() {
        Calendar calendar = Calendar.getInstance(); // 현재 날짜
        calendar.add(Calendar.YEAR, 5);
        Long accountId = cardMapper.getAccountId("222-2222-2222", "1469a2a3-213d-427e-b29f-f79d58f51190");
        System.out.println("accountId = " + accountId);
        UserCardVO card = UserCardVO.builder()
                .userId("1469a2a3-213d-427e-b29f-f79d58f51190")
                .productId(2003L)
                .monthlyUsage(0)
                .issueDate(new Date())
                .expiryDate(calendar.getTime())
                .performanceCondition(150000)
                .cardNumber("1234-5678-0000-1111")
                .accountId(accountId)
                .build();

        cardMapper.insertUserCard(card);
        long id = card.getId();
        System.out.println("id = " + id);
        cardMapper.updateUserCardInfo(id, card.getUserId());
        String myCardIds = cardMapper.getMyCardIdsJson(card.getUserId());
        System.out.println("myCardIds = " + myCardIds);
        UserCardVO cardCheck = cardMapper.findUserCardByproductId(card.getProductId(), card.getUserId());
        assertNotNull(cardCheck);
        System.out.println("card = " + cardCheck);
    }

    @Test
    @DisplayName("[성공] 추천을 위한 사용자 카드 정보 조회")
    void findUserInfoForCardRecommend() {

        // when
        UserInfoForCardDTO userInfoForCard = cardMapper.findUserInfoForCardRecommend(userId);

        // then
        assertNotNull(userInfoForCard);
        log.info("userInfoForCard = " + userInfoForCard);
    }

    @Test
    @DisplayName("[성공] 카드 전체 목록 조회")
    void findAllCardProducts() {
        // given & when
        List<CardProductVO> cardProductVOList = cardMapper.findAllCardProducts();

        // then
        assertNotNull(cardProductVOList);
        log.info("cardProductVOList = " + cardProductVOList);
    }

    @Test
    @DisplayName("[성공] 사용자가 보유한 카드 상품 Id 조회")
    void findProductIdsByUserCardId() {
        // given
        List<Long> myCardIds = List.of(1L, 14L);

        // when
        List<Long> myProductIds = cardMapper.findProductIdsByUserCardIds(myCardIds);

        // then
        assertNotNull(myProductIds);

        log.info("myProductIds = " + myProductIds);

    }
}
