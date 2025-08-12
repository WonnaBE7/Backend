package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.dto.MonthlyInsuranceReceiptDto;
import com.wonnabe.product.dto.UserInsuranceDetailDTO;
import com.wonnabe.product.mapper.UserInsuranceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
class UserInsuranceServiceImplTest {

    @Mock
    private UserInsuranceMapper userInsuranceMapper;
    @Mock
    private Clock clock; // Mock Clock

    @InjectMocks
    private UserInsuranceServiceImpl userInsuranceService;

    private UserInsuranceVO mockUserInsurance;

    @BeforeEach
    void setUp() {
        // Fixed date for testing LocalDate.now(clock)
        Instant fixedInstant = Instant.parse("2025-08-12T10:00:00Z");
        ZoneId zoneId = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(zoneId);

        // Mock InsuranceProductVO
        InsuranceProductVO mockProduct = InsuranceProductVO.builder()
                .productId(3001L)
                .providerName("테스트보험사")
                .productName("든든 건강보험")
                .build();

        // Mock UserInsuranceVO
        mockUserInsurance = UserInsuranceVO.builder()
                .id(1L)
                .userId("user123")
                .productId(3001L)
                .monthlyPremium(new BigDecimal("100000"))
                .startDate(java.sql.Date.valueOf(java.time.LocalDate.now(clock).minusMonths(6))) // Use clock here
                .endDate(java.sql.Date.valueOf(java.time.LocalDate.now(clock).plusMonths(6))) // Use clock here
                .totalPaid(new BigDecimal("600000")) // Example total paid
                .product(mockProduct) // Set the mocked product
                .build();
    }

    @Test
    @DisplayName("보험 상세 정보 조회 시, DTO로 정확하게 변환되어야 한다.")
    void getDetailByProductId_should_return_dto_when_data_exists() {
        // given: Mock Mapper가 mockUserInsurance 객체를 반환하도록 설정
        String userId = "user123";
        Long productId = 3001L; // Use the productId from mockUserInsurance

        // Mock mapper calls
        when(userInsuranceMapper.findDetailByProductId(userId, productId)).thenReturn(mockUserInsurance);
        when(userInsuranceMapper.findInsuranceProductById(productId)).thenReturn(mockUserInsurance.getProduct()); // Return the product from mockUserInsurance
        when(userInsuranceMapper.findTotalReceiptAmount(userId, productId)).thenReturn(500000L); // Example total receipt amount
        when(userInsuranceMapper.findTotalPaymentAmount(userId, productId)).thenReturn(600000L); // Example total payment amount

        // Mock monthly receipts
        List<MonthlyInsuranceReceiptDto> mockMonthlyReceipts = new ArrayList<>();
        // Mock data for the full 12-month range (Sep 2024 to Aug 2025)
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2024-09", 100000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2024-10", 150000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2024-11", 120000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2024-12", 180000L));

        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-01", 90000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-02", 110000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-03", 130000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-04", 160000L));

        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-05", 140000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-06", 170000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-07", 100000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-08", 190000L));

        // Add more as needed for your test scenario
        when(userInsuranceMapper.findMonthlyInsuranceReceipts(eq(userId), eq(productId), any(java.sql.Date.class))).thenReturn(mockMonthlyReceipts);


        // when: 서비스 메소드 호출
        UserInsuranceDetailDTO responseDTO = userInsuranceService.getDetailByProductId(userId, productId);

        // then: 결과 검증
        assertNotNull(responseDTO, "응답 DTO는 null이 아니어야 합니다.");
        assertEquals(String.valueOf(productId), responseDTO.getProductId(), "상품 ID가 일치해야 합니다.");
        assertEquals("든든 건강보험", responseDTO.getInsuranceName(), "보험 상품명이 일치해야 합니다.");
        assertEquals("테스트보험사", responseDTO.getInsuranceCompany(), "보험 회사명이 일치해야 합니다.");

        // Verify calculated fields
        assertEquals("83.33%", responseDTO.getAchievementRate(), "달성률이 올바르게 계산되어야 합니다."); // (500000 / 600000) * 100 = 83.333...
        assertEquals("500000", responseDTO.getGetAmount(), "총 수령액이 일치해야 합니다.");
        assertEquals("600000", responseDTO.getCurrentAmount(), "총 납입액이 일치해야 합니다."); // This comes from UserInsuranceVO.totalPaid

        // Verify monthly chart
        assertNotNull(responseDTO.getMonthlyChart(), "월별 차트 데이터는 null이 아니어야 합니다.");
        assertFalse(responseDTO.getMonthlyChart().isEmpty(), "월별 차트 데이터는 비어있지 않아야 합니다.");
        assertEquals(12, responseDTO.getMonthlyChart().size(), "월별 차트 데이터 개수가 일치해야 합니다."); // Corrected size to 12
        assertEquals("9월", responseDTO.getMonthlyChart().get(0).getMonth(), "첫 번째 월이 일치해야 합니다."); // Corrected month
        assertEquals(100000L, responseDTO.getMonthlyChart().get(0).getAmount(), "첫 번째 월의 금액이 일치해야 합니다.");
        assertEquals("10월", responseDTO.getMonthlyChart().get(1).getMonth(), "두 번째 월이 일치해야 합니다."); // Corrected month
        assertEquals(150000L, responseDTO.getMonthlyChart().get(1).getAmount(), "두 번째 월의 금액이 일치해야 합니다.");
        // Add more assertions for other months if desired, or iterate through the list.

        // Verify term calculation
        assertNotNull(responseDTO.getTerm(), "납입 기간은 null이 아니어야 합니다.");
        assertEquals("12개월", responseDTO.getTerm(), "납입 기간이 일치해야 합니다."); // Added precise assertion
}
    @Test
    @DisplayName("조회된 데이터가 없을 경우, null을 반환해야 한다.")
    void getDetailByProductId_should_return_null_when_no_data() {
        // given: Mock Mapper가 null을 반환하도록 설정
        String userId = "user123";
        Long nonExistentProductId = 9999L;
        when(userInsuranceMapper.findDetailByProductId(userId, nonExistentProductId)).thenReturn(null);

        // when: 서비스 메소드 호출
        UserInsuranceDetailDTO responseDTO = userInsuranceService.getDetailByProductId(userId, nonExistentProductId);

        // then: 결과 검증
        assertNull(responseDTO, "응답 DTO는 null이어야 합니다.");
    }
}
