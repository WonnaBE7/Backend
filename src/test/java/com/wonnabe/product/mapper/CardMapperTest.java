package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.MonthlyConsumptionDTO;
import com.wonnabe.product.dto.UserCardDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class
})
public class CardMapperTest {
    @Autowired
    private CardMapper cardMapper;
    // 카드 상품 정보 가져오는 거 테스트
    @Test
    void testFindByProductId() {
        CardProductVO cardProductVO = cardMapper.findById(3001);
        System.out.println(cardProductVO);
    }

    // 사용자 카드 정보 가져오는거 테스트
    @Test
    void testFindUserCardByproductId() {

        UserCardVO userCardVO = cardMapper.findUserCardByproductId(3001, "1469a2a3-213d-427e-b29f-f79d58f51190");
        System.out.println("usercardVO = " + userCardVO);
    }


    // 사용자 카드와 거래내역 join한거 테스트
    @Test
    void testFindUserCardDetailByProductId() {
        UserCardDTO userCardDTO = cardMapper.findUserCardDetailById(3001, "1469a2a3-213d-427e-b29f-f79d58f51190");
        System.out.println("usercardDTO = " + userCardDTO);
        for (MonthlyConsumptionDTO m : userCardDTO.getConsumptions()) {
            System.out.println("month: " + m);
        }
    }
}
