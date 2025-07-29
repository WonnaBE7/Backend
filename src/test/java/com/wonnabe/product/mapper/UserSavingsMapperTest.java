package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class}) // RootConfig 경로가 맞는지 확인 필요
@Log4j2
class UserSavingsMapperTest {

    @Autowired
    private UserSavingsMapper userSavingsMapper;

    private final String userId = "550e8400-e29b-41d4-a716-446655440000";
    private final Long productId = 1306L;

    @Test
    @DisplayName("사용자 예적금 상세 정보를 성공적으로 조회한다")
    void findSavingsDetailByIds_success() {
        // when
        UserSavingsVO result = userSavingsMapper.findSavingsDetailByIds(userId, productId);

        // then
        assertNotNull(result, "조회 결과는 null이 아니어야 합니다.");
        assertEquals(userId, result.getUserId());
        assertEquals(productId, result.getProductId());

        // 연관된 Product 정보 확인
        assertNotNull(result.getSavingsProduct(), "상품 정보는 null이 아니어야 합니다.");
        assertNotNull(result.getSavingsProduct().getProductName());

        log.info("=== 예적금 상세 조회 (Mapper) 성공 ===");
        log.info("조회된 정보: {}", result);
    }

    @Test
    @DisplayName("월별 거래 내역 합계를 성공적으로 조회한다")
    void findMonthlyTransactionSums_success() {
        // given
        // 테스트를 위해 해당 사용자의 가장 오래된 적금 거래 날짜를 알아야 함
        // 우선 1년 전으로 가정하고 테스트
        Date startDate = new Date(System.currentTimeMillis() - 31536000000L);

        // when
        List<TransactionSummaryDto> result = userSavingsMapper.findMonthlyTransactionSums(userId, startDate);

        // then
        assertNotNull(result, "조회 결과는 null이 아니어야 합니다.");
        // DB에 실제 데이터가 있다면, 리스트가 비어있지 않아야 함
        // assertTrue(result.size() > 0, "거래 내역이 하나 이상 있어야 합니다.");

        log.info("=== 월별 거래 내역 조회 (Mapper) 성공 ===");
        result.forEach(dto -> log.info("월: {}, 합계: {}", dto.getMonth(), dto.getTotalSavings()));
    }
}
