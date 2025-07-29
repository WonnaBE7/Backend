package com.wonnabe.product.service;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.dto.UserCardDetailDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@Log4j2
class CardServiceImplTest {

    @Autowired
    private CardService cardService;

    // 유저 정보 가져와서 출력하여 테스트
    @Test
    void findUserCardDetail() {
        UserCardDetailDTO userCardDetailDTO = cardService.findUserCardDetail(3001, "1469a2a3-213d-427e-b29f-f79d58f51190");
        System.out.println("userCardDetailDTO = " + userCardDetailDTO);
    }
}