package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.BasicUserInfoDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Mapper 계층 테스트
 * - @ExtendWith(SpringExtension.class): JUnit5에서 Spring 테스트 컨텍스트를 사용합니다.
 * - @ContextConfiguration: Spring 설정 파일을 지정하여 ApplicationContext를 생성합니다.
 * - 실제 데이터베이스에 연결하여 SQL 쿼리가 정상적으로 동작하는지 검증합니다.
 *   (테스트 프로파일의 DB 설정을 따릅니다.)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@Log4j2
class ProductDetailMapperTest {

    // 테스트 대상인 ProductMapper를 Spring 컨테이너로부터 주입받습니다.
    @Autowired
    private ProductDetailMapper productDetailMapper;

    // 테스트에 사용할 사용자 ID (테스트 DB에 해당 사용자가 존재해야 합니다.)
    private final String userId = "1469a2a3-213d-427e-b29f-f79d58f51190";

    @Test
    @DisplayName("[성공] 상품 ID로 예적금 상품 정보 조회")
    void findSavingProductById_success() {
        // given: 테스트 사전 조건 설정
        String productId = "1300"; // 테스트 DB에 존재하는 상품 ID

        // when: 테스트 대상 메서드 호출
        SavingsProductVO result = productDetailMapper.findSavingProductById(productId);

        // then: 결과 검증 (JUnit 5 Assertions 사용)
        log.info("조회된 예적금 상품 정보: {}", result);
        assertNotNull(result, "조회 결과는 null이 아니어야 합니다.");
        assertEquals(Long.valueOf(productId), result.getProductId(), "조회된 상품 ID가 요청한 ID와 일치해야 합니다.");
        assertNotNull(result.getProductName(), "상품 이름은 null이 아니어야 합니다.");
    }

    @Test
    @DisplayName("[성공] 사용자 ID로 기본 사용자 정보(찜 목록, 페르소나 등) 조회")
    void findBasicUserInfoById_success() {
        // given
        // userId는 필드에 정의된 값을 사용합니다.

        // when
        BasicUserInfoDTO result = productDetailMapper.findBasicUserInfoById(userId);

        // then
        log.info("조회된 사용자 기본 정보: {}", result);
        assertNotNull(result, "조회 결과는 null이 아니어야 합니다.");
        assertEquals(userId, result.getUserId(), "조회된 사용자 ID가 요청한 ID와 일치해야 합니다.");
        assertNotNull(result.getFavoriteProductsByType(), "찜 목록 정보는 null이 아니어야 합니다.");
        assertNotNull(result.getNowMeId(), "페르소나 ID 정보는 null이 아니어야 합니다.");
    }

    @Test
    @DisplayName("[성공] 상품 ID로 보험 상품 정보 조회")
    void findInsuranceProductById_success() {
        // given: 테스트 사전 조건 설정
        String productId = "3001"; // 테스트 DB에 존재하는 보험 상품 ID

        // when: 테스트 대상 메서드 호출
        InsuranceProductVO result = productDetailMapper.findInsuranceProductById(productId);

        // then: 결과 검증 (JUnit 5 Assertions 사용)
        log.info("조회된 보험 상품 정보: {}", result);
        assertNotNull(result, "조회 결과는 null이 아니어야 합니다.");
        assertEquals(Long.valueOf(productId), result.getProductId(), "조회된 상품 ID가 요청한 ID와 일치해야 합니다.");
        assertNotNull(result.getProductName(), "상품 이름은 null이 아니어야 합니다.");
    }
}
