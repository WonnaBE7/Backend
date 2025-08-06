package com.wonnabe.product.mapper;

import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.wonnabe.product.dto.TransactionSummaryDto;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "springfox.documentation.enabled=false"
})
@Transactional
class UserInsuranceMapperTest {

    @Autowired
    private UserInsuranceMapper userInsuranceMapper;

    // 월별 테스트할때의 TestUserID
    private final String testUserId = "9e423205-426c-442e-96a6-170a27ad3f8d";

    @Test
    @DisplayName("사용자 ID와 상품 ID로 상세 정보를 정상적으로 조회해야 한다.")
    void findDetailByProductId_should_return_detail() {
        // given: 테스트 DB에 존재하는 사용자 ID와 상품 ID
        // 실제 테스트 환경에 맞는 유효한 userId와 productId를 사용해야 합니다.
        String userId = "b2c3d4e5-f678-9012-abcd-ef12gh34ij56";
        Long productId = 3002L;

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

    @Test
    @DisplayName("월별 보험 거래 내역 합계를 정상적으로 조회해야 한다.")
    void findMonthlyTransactionSums_should_return_correct_sums() {
        // given
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        // when
        List<TransactionSummaryDto> summaries = userInsuranceMapper.findMonthlyTransactionSums(testUserId, Date.valueOf(startDate));


        // 1월 데이터 검증
        TransactionSummaryDto janSummary = summaries.stream()
                .filter(s -> s.getMonth().equals("2024-01"))
                .findFirst()
                .orElse(null);
        assertNotNull(janSummary, "1월 요약이 존재해야 합니다.");
        assertEquals(-15000L, janSummary.getTotalSavings(), "1월 총액은 -15000이어야 합니다.");

        // 2월 데이터 검증
        TransactionSummaryDto febSummary = summaries.stream()
                .filter(s -> s.getMonth().equals("2024-02"))
                .findFirst()
                .orElse(null);
        assertNotNull(febSummary, "2월 요약이 존재해야 합니다.");
        assertEquals(-12000L, febSummary.getTotalSavings(), "2월 총액은 -12000이어야 합니다.");

        // 3월 데이터 검증
        TransactionSummaryDto marSummary = summaries.stream()
                .filter(s -> s.getMonth().equals("2024-03"))
                .findFirst()
                .orElse(null);
        assertNotNull(marSummary, "3월 요약이 존재해야 합니다.");
        assertEquals(-8000L, marSummary.getTotalSavings(), "3월 총액은 -8000이어야 합니다.");

        System.out.println("===== 월별 거래 내역 합계 =====");
        summaries.forEach(s -> System.out.printf(
                "월: %s | 총액: %d%n",
                s.getMonth(), s.getTotalSavings()
        ));
        System.out.println("==========================");

    }
}
