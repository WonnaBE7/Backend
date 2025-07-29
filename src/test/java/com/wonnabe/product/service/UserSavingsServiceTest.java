package com.wonnabe.product.service;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@Transactional // 테스트 후 DB 롤백
@Log4j2
class UserSavingsServiceTest {

    @Autowired
    private UserSavingsService userSavingsService;

    private final String userId = "550e8400-e29b-41d4-a716-446655440000";
    private final Long productId = 1306L; // 테스트에 사용할 상품 ID (적금 또는 예금)

    @Test
    @DisplayName("상품 상세 정보를 성공적으로 조회하고, 타입에 따라 적금/예금으로 분류한다")
    void getSavingsDetail_success() {
        // when
        UserSavingsDetailResponseDto result = userSavingsService.getSavingsDetail(userId, productId);

        // then
        assertNotNull(result);
        assertEquals(String.valueOf(productId), result.getProductId());

        // 서비스 로직에 따라 '적금' 또는 '예금'으로 분류되는지 확인
        // 이 부분은 실제 DB 데이터의 monthlyPayment 값에 따라 달라집니다.
        // monthlyPayment > 0 이면 '적금', 0 이면 '예금'
        if (result.getProductType().equals("적금")) {
            assertNotNull(result.getMonthlyChart());
            assertFalse(result.getMonthlyChart().isEmpty(), "적금은 월별 차트가 비어있지 않아야 합니다.");
        } else if (result.getProductType().equals("예금")) {
            assertNotNull(result.getMonthlyChart());
            assertTrue(result.getMonthlyChart().isEmpty(), "예금은 월별 차트가 비어있어야 합니다.");
            assertEquals(100, result.getAchievementRate());
        }

        assertNotNull(result.getAchievementRate());

        log.info("=== 상품 상세 조회 (Service) 성공 ===");
        log.info("조회 결과: {}", result);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 null을 반환한다")
    void getSavingsDetail_forNonExistent_returnsNull() {
        // given
        Long nonExistentProductId = 9999L;

        // when
        UserSavingsDetailResponseDto result = userSavingsService.getSavingsDetail(userId, nonExistentProductId);

        // then
        assertNull(result);
        log.info("=== 존재하지 않는 상품 조회 테스트 성공 ===");
    }
}