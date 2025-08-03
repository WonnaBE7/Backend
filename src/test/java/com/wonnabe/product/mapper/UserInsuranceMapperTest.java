package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@TestPropertySource(properties = {
        "springfox.documentation.enabled=false"
})
@Transactional
class UserInsuranceMapperTest {

    @Autowired
    private UserInsuranceMapper userInsuranceMapper;

    @Test
    @DisplayName("사용자 ID와 상품 ID로 상세 정보를 정상적으로 조회해야 한다.")
    void findDetailByProductId_should_return_detail() {
        // given: 테스트 DB에 존재하는 사용자 ID와 상품 ID
        // 실제 테스트 환경에 맞는 유효한 userId와 productId를 사용해야 합니다.
        String userId = "user123"; // 예시: 실제 DB에 존재하는 사용자 ID
        Long productId = 1L; // 예시: 실제 DB에 존재하는 보험 상품 ID

        // when: Mapper 메소드 호출
        UserInsuranceVO userInsurance = userInsuranceMapper.findDetailByProductId(userId, productId);

        // then: 반환된 결과 검증
        assertNotNull(userInsurance, "결과는 null이 아니어야 합니다.");
        assertEquals(userId, userInsurance.getUserId(), "조회된 userId가 일치해야 합니다.");
        assertEquals(productId, userInsurance.getProductId(), "조회된 productId가 일치해야 합니다.");

        InsuranceProductVO product = userInsurance.getProduct();
        assertNotNull(product, "상품 정보는 null이 아니어야 합니다.");
        assertNotNull(product.getProductName(), "상품명 정보가 있어야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시, null을 반환해야 한다.")
    void findDetailByProductId_with_non_existent_id_should_return_null() {
        // given: 존재하지 않는 ID
        String userId = "user123";
        Long nonExistentProductId = 9999L;

        // when: Mapper 메소드 호출
        UserInsuranceVO userInsurance = userInsuranceMapper.findDetailByProductId(userId, nonExistentProductId);

        // then: 반환된 결과 검증
        assertNull(userInsurance, "결과는 null이어야 합니다.");
    }
}
