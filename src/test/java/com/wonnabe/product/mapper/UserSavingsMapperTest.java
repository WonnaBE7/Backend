package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@TestPropertySource(properties = {
        "springfox.documentation.enabled=false"  // 테스트에서 Swagger 비활성화
})
@Transactional // 각 테스트 후 롤백을 위해 추가
public class UserSavingsMapperTest {

    @Autowired
    private UserSavingsMapper userSavingsMapper;

    private final String userId = "550e8400-e29b-41d4-a716-446655440000";
    private final Long productId = 1306L;

    @Test
    @DisplayName("사용자 예적금 상세 정보 조회 - 성공")
    void testFindSavingsDetailByIds() {
        // when
        UserSavingsVO userSavingsVO = userSavingsMapper.findSavingsDetailByIds(userId, productId);
        
        // then
        assertNotNull(userSavingsVO);
        assertEquals(userId, userSavingsVO.getUserId());
        assertEquals(productId, userSavingsVO.getProductId());
        System.out.println("userSavingsVO = " + userSavingsVO);
    }

    @Test
    @DisplayName("특정 상품에 대한 월별 거래 내역 합계 조회 - 성공")
    void testFindMonthlyTransactionSums() {
        // given
        // 참고: 이 테스트는 테스트 DB에 userId와 productId에 해당하는 거래 내역이 존재한다고 가정합니다.
        Date startDate = Date.from(Instant.parse("2020-01-01T00:00:00Z"));

        // when
        List<TransactionSummaryDto> transactionSummaryList = userSavingsMapper.findMonthlyTransactionSums(userId, productId, startDate);
        
        // then
        assertNotNull(transactionSummaryList);
        assertFalse(transactionSummaryList.isEmpty(), "거래 내역이 존재해야 합니다.");
        
        System.out.println("transactionSummaryList for product " + productId + " = " + transactionSummaryList);
        transactionSummaryList.forEach(dto -> {
            System.out.println("month: " + dto.getMonth() + ", totalSavings: " + dto.getTotalSavings());
            assertNotNull(dto.getMonth());
            assertNotNull(dto.getTotalSavings());
        });
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 상세 조회 - null 반환")
    void testFindSavingsDetailByIds_NotFound() {
        // given
        String nonExistentUserId = "non-existent-user-id";
        
        // when
        UserSavingsVO userSavingsVO = userSavingsMapper.findSavingsDetailByIds(nonExistentUserId, productId);
        
        // then
        assertNull(userSavingsVO);
        System.out.println("notFound userSavingsVO = " + userSavingsVO);
    }
}
