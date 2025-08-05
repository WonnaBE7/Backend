package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class, RedisConfig.class
})
@ActiveProfiles("test") // 테스트용 프로파일을 사용하여 테스트 DB를 사용하도록 설정
public class SavingsMapperTest {

    @Autowired
    private SavingsMapper savingsMapper;

    private final String TEST_USER_ID = "1469a2a3-213d-427e-b29f-f79d58f51190";
    private final long TEST_PRODUCT_ID = 1310L;

    @Test
    @DisplayName("예적금 ID로 상품 정보 조회 테스트")
    void findById() {
        // when: 매퍼를 통해 예적금 상품 정보를 조회
        SavingsProductVO product = savingsMapper.findById(TEST_PRODUCT_ID);

        // then: 조회된 객체가 null이 아니고, 요청한 ID와 일치하는지 확인
        assertNotNull(product);
        assertEquals(TEST_PRODUCT_ID, product.getProductId());
        System.out.println("조회된 예적금 상품: " + product);
    }

    @Test
    @Transactional // 테스트 후 DB 변경사항을 롤백
    @DisplayName("예적금 가입 및 사용자 정보 업데이트 통합 테스트")
    void applySavings_integration_test() {
        // given: 테스트를 위한 사용자 예적금 가입 정보 준비
        Date startDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, 12);
        Date maturityDate = calendar.getTime();

        UserSavingsVO newSavings = UserSavingsVO.builder()
                .userId(TEST_USER_ID)
                .productId(TEST_PRODUCT_ID)
                .principalAmount(1000000L)
                .startDate(startDate)
                .maturityDate(maturityDate)
                .monthlyPayment(100000L)
                .build();

        // when: 1. 사용자 예적금 정보 삽입
        savingsMapper.insertUserSavings(newSavings);
        long newId = newSavings.getId();

        // then: 1. 삽입 후 ID가 정상적으로 생성되었는지 확인
        assertTrue(newId > 0);

        // when: 2. 사용자 정보에 예적금 ID 업데이트
        savingsMapper.updateUserSavingsInfo(newId, TEST_USER_ID);

        // then: 2. 업데이트 후, 해당 사용자의 예적금 목록에 새 ID가 포함되어 있는지 확인
        String mySavingsIdsJson = savingsMapper.getMySavingsIdsJson(TEST_USER_ID);
        assertNotNull(mySavingsIdsJson);
        assertTrue(mySavingsIdsJson.contains(String.valueOf(newId)));

        // then: 3. 최종적으로 가입된 예적금 정보가 DB에서 정상적으로 조회되는지 확인
        UserSavingsVO savedSavings = savingsMapper.findUserSavingsByProductId(TEST_PRODUCT_ID, TEST_USER_ID);
        assertNotNull(savedSavings);
        assertEquals(newId, savedSavings.getId());

        System.out.println("성공적으로 가입된 예적금 정보: " + savedSavings);
    }
}
