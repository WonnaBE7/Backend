package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class, RedisConfig.class
})
@ActiveProfiles("test") // 테스트용 프로파일을 사용하여 테스트 DB를 사용하도록 설정
public class InsuranceMapperTest {

    @Autowired
    private InsuranceMapper insuranceMapper;

    private final String TEST_USER_ID = "b2c3d4e5-f678-9012-abcd-ef12gh34ij56";
    private final long TEST_PRODUCT_ID = 3002L;

    @Test
    @DisplayName("보험 ID로 상품 정보 조회 테스트")
    void findById() {
        // when: 매퍼를 통해 보험 상품 정보를 조회
        InsuranceProductVO product = insuranceMapper.findById(TEST_PRODUCT_ID);

        // then: 조회된 객체가 null이 아니고, 요청한 ID와 일치하는지 확인
        assertNotNull(product);
        assertEquals(TEST_PRODUCT_ID, product.getProductId());
        System.out.println("조회된 보험 상품: " + product);
    }

    @Test
    @DisplayName("사용자 ID로 성별 정보 조회 테스트")
    void findGenderByUserId() {
        // when: 매퍼를 통해 사용자의 성별을 조회
        String gender = insuranceMapper.findGenderByUserId(TEST_USER_ID);

        // then: 조회된 성별이 null이 아니고, "M" 또는 "F"인지 확인
        assertNotNull(gender);
        assertTrue(gender.equals("M") || gender.equals("F"));
        System.out.println("조회된 사용자 성별: " + gender);
    }

    @Test
    @Transactional // 테스트 후 DB 변경사항을 롤백
    @DisplayName("보험 가입 및 사용자 정보 업데이트 통합 테스트")
    void applyInsurance_integration_test() {
        // given: 테스트를 위한 사용자 보험 가입 정보 준비
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 10);
        UserInsuranceVO newInsurance = UserInsuranceVO.builder()
                .userId(TEST_USER_ID)
                .productId(TEST_PRODUCT_ID)
                .startDate(new Date(System.currentTimeMillis()))
                .endDate(new Date(calendar.getTimeInMillis()))
                .monthlyPremium(new BigDecimal("50000"))
                .build();

        // when: 1. 사용자 보험 정보 삽입
        insuranceMapper.insertUserInsurance(newInsurance);
        long newId = newInsurance.getId();

        // then: 1. 삽입 후 ID가 정상적으로 생성되었는지 확인
        assertTrue(newId > 0);

        // when: 2. 사용자 정보에 보험 ID 업데이트
        insuranceMapper.updateUserInsuranceInfo(newId, TEST_USER_ID);

        // then: 2. 업데이트 후, 해당 사용자의 보험 목록에 새 ID가 포함되어 있는지 확인
        String myInsuranceIdsJson = insuranceMapper.getMyInsuranceIdsJson(TEST_USER_ID);
        assertNotNull(myInsuranceIdsJson);
        assertTrue(myInsuranceIdsJson.contains(String.valueOf(newId)));

        // then: 3. 최종적으로 가입된 보험 정보가 DB에서 정상적으로 조회되는지 확인
        UserInsuranceVO savedInsurance = insuranceMapper.findUserInsuranceByProductId(TEST_PRODUCT_ID, TEST_USER_ID);
        assertNotNull(savedInsurance);
        assertEquals(newId, savedInsurance.getId());

        System.out.println("성공적으로 가입된 보험 정보: " + savedInsurance);
    }
}
