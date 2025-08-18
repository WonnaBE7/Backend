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

@ExtendWith(MockitoExtension.class)
class UserInsuranceServiceImplTest {

    @Mock
    private UserInsuranceMapper userInsuranceMapper;

    @InjectMocks
    private UserInsuranceServiceImpl userInsuranceService;

    private UserInsuranceVO mockUserInsurance;

    @BeforeEach
    void setUp() {
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
                .startDate(java.sql.Date.valueOf(java.time.LocalDate.now().minusMonths(6)))
                .endDate(java.sql.Date.valueOf(java.time.LocalDate.now().plusMonths(6)))
                .totalPaid(new BigDecimal("500000")) // 5개월 납입 총액
                .product(mockProduct)
                .build();
    }

    @Test
    @DisplayName("보험 상세 정보 조회 시, DTO로 정확하게 변환되어야 한다.")
    void getDetailByProductId_should_return_dto_when_data_exists() {
        // given: Mock Mapper가 mockUserInsurance 객체를 반환하도록 설정
        String userId = "user123";
        Long productId = 3001L;

        // Mock mapper calls
        when(userInsuranceMapper.findDetailByProductId(userId, productId)).thenReturn(mockUserInsurance);
        when(userInsuranceMapper.findInsuranceProductById(productId)).thenReturn(mockUserInsurance.getProduct());
        when(userInsuranceMapper.findTotalReceiptAmount(userId, productId)).thenReturn(400000L); // 5개월 총 수령액
        when(userInsuranceMapper.findTotalPaymentAmount(userId, productId)).thenReturn(500000L); // 5개월 총 납입액

        // Mock monthly receipts - 최근 5달 데이터 (2025-04 ~ 2025-08)
        List<MonthlyInsuranceReceiptDto> mockMonthlyReceipts = new ArrayList<>();
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-04", 80000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-05", 90000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-06", 75000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-07", 85000L));
        mockMonthlyReceipts.add(new MonthlyInsuranceReceiptDto("2025-08", 70000L));

        when(userInsuranceMapper.findMonthlyInsuranceReceipts(eq(userId), eq(productId), any(java.sql.Date.class))).thenReturn(mockMonthlyReceipts);

        // when: 서비스 메소드 호출
        UserInsuranceDetailDTO responseDTO = userInsuranceService.getDetailByProductId(userId, productId);

        // then: 결과 검증
        assertNotNull(responseDTO, "응답 DTO는 null이 아니어야 합니다.");
        assertEquals(String.valueOf(productId), responseDTO.getProductId(), "상품 ID가 일치해야 합니다.");
        assertEquals("든든 건강보험", responseDTO.getInsuranceName(), "보험 상품명이 일치해야 합니다.");
        assertEquals("테스트보험사", responseDTO.getInsuranceCompany(), "보험 회사명이 일치해야 합니다.");

        // Verify calculated fields
        // 달성률: (오늘 - 시작일) / (종료일 - 시작일) * 100
        // 시작일: 6개월 전, 종료일: 6개월 후 => 총 12개월
        // 경과 기간: 6개월
        // (6 / 12) * 100 = 50
        assertEquals(49, responseDTO.getAchievementRate(), "달성률이 올바르게 계산되어야 합니다.");
        assertEquals("400000", responseDTO.getGetAmount(), "총 수령액이 일치해야 합니다.");
        assertEquals("500000", responseDTO.getCurrentAmount(), "총 납입액이 일치해야 합니다.");

        // Verify monthly chart
        assertNotNull(responseDTO.getMonthlyChart(), "월별 차트 데이터는 null이 아니어야 합니다.");
        assertFalse(responseDTO.getMonthlyChart().isEmpty(), "월별 차트 데이터는 비어있지 않아야 합니다.");
        assertEquals(5, responseDTO.getMonthlyChart().size(), "월별 차트 데이터 개수가 일치해야 합니다.");
        assertEquals("4월", responseDTO.getMonthlyChart().get(0).getMonth(), "첫 번째 월이 일치해야 합니다.");
        assertEquals(80000L, responseDTO.getMonthlyChart().get(0).getAmount(), "첫 번째 월의 금액이 일치해야 합니다.");
        assertEquals("5월", responseDTO.getMonthlyChart().get(1).getMonth(), "두 번째 월이 일치해야 합니다.");
        assertEquals(90000L, responseDTO.getMonthlyChart().get(1).getAmount(), "두 번째 월의 금액이 일치해야 합니다.");
        assertEquals("8월", responseDTO.getMonthlyChart().get(4).getMonth(), "마지막 월이 일치해야 합니다.");
        assertEquals(70000L, responseDTO.getMonthlyChart().get(4).getAmount(), "마지막 월의 금액이 일치해야 합니다.");

        // Verify term calculation
        assertNotNull(responseDTO.getTerm(), "납입 기간은 null이 아니어야 합니다.");
        assertEquals("12개월", responseDTO.getTerm(), "납입 기간이 일치해야 합니다.");
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