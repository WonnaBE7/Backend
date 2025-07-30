package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.common.security.config.SecurityConfig;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@TestPropertySource(properties = {
        "springfox.documentation.enabled=false"  // 테스트에서 Swagger 비활성화
})
public class UserSavingsMapperTest {

    @Autowired
    private UserSavingsMapper userSavingsMapper;

    private final String userId = "550e8400-e29b-41d4-a716-446655440000";
    private final Long productId = 1306L;

    // 사용자 예적금 상세 정보 가져오는 거 테스트
    @Test
    void testFindSavingsDetailByIds() {
        UserSavingsVO userSavingsVO = userSavingsMapper.findSavingsDetailByIds(userId, productId);
        System.out.println("userSavingsVO = " + userSavingsVO);
    }

    // 월별 거래 내역 합계 가져오는거 테스트
    @Test
    void testFindMonthlyTransactionSums() {
        Date startDate = new Date(0); // 1970-01-01
        List<TransactionSummaryDto> transactionSummaryList = userSavingsMapper.findMonthlyTransactionSums(userId, startDate);
        System.out.println("transactionSummaryList = " + transactionSummaryList);

        for (TransactionSummaryDto dto : transactionSummaryList) {
            System.out.println("month: " + dto.getMonth() + ", totalSavings: " + dto.getTotalSavings());
        }
    }

    // 존재하지 않는 사용자 ID로 조회하는거 테스트
    @Test
    void testFindSavingsDetailByIds_NotFound() {
        String nonExistentUserId = "non-existent-user-id";
        UserSavingsVO userSavingsVO = userSavingsMapper.findSavingsDetailByIds(nonExistentUserId, productId);
        System.out.println("notFound userSavingsVO = " + userSavingsVO);
    }
}