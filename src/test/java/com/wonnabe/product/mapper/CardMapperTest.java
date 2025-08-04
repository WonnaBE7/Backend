package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.MonthlyConsumptionDTO;
import com.wonnabe.product.dto.UserCardDTO;
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
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class, RedisConfig.class
})
@ActiveProfiles("test")
public class CardMapperTest {
    @Autowired
    private CardMapper cardMapper;
    // 카드 상품 정보 가져오는 거 테스트
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
        UserCardVO userCardVO = cardMapper.findUserCardByproductId(2001, "1469a2a3-213d-427e-b29f-f79d58f51190");
        assertNotNull(userCardVO);
        assertEquals(2001, userCardVO.getProductId());
        assertEquals("1469a2a3-213d-427e-b29f-f79d58f51190", userCardVO.getUserId());
        System.out.println("usercardVO = " + userCardVO);
    }


    // 사용자 카드와 거래내역 join한거 테스트
    @Test
    void testFindUserCardDetailByProductId() {
        UserCardDTO userCardDTO = cardMapper.findUserCardDetailById(2001, "1469a2a3-213d-427e-b29f-f79d58f51190");
        assertNotNull(userCardDTO);
        System.out.println("usercardDTO = " + userCardDTO);
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
}
