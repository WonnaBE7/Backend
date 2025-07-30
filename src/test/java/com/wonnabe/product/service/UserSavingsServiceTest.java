package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.TransactionSummaryDto;
import com.wonnabe.product.dto.UserSavingsDetailResponseDto;
import com.wonnabe.product.mapper.UserSavingsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * UserSavingsService의 비즈니스 로직을 단위 테스트하는 클래스입니다.
 * Mockito를 사용하여 매퍼의 의존성을 제거하고, 순수 서비스 로직만을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class UserSavingsServiceTest {

    @Mock // 가짜(Mock) 매퍼 객체 생성
    private UserSavingsMapper userSavingsMapper;

    @InjectMocks // @Mock으로 생성된 객체를 주입받는 서비스 객체 생성
    private UserSavingsService userSavingsService;

    private final String userId = "testUser";

    @Test
    @DisplayName("적금 상품: 이자를 포함한 최근 5개월 자산 차트와 최종 달성률을 정확히 계산한다")
    void getSavingsDetail_forSavingsProduct_success() {
        // given: 테스트를 위한 가짜 데이터 설정
        Long productId = 1L;
        // UserSavingsMapper가 특정 값을 반환하도록 미리 정의합니다.
        given(userSavingsMapper.findSavingsDetailByIds(userId, productId)).willReturn(createMockSavingsVO(productId));
        given(userSavingsMapper.findMonthlyTransactionSums(anyString(), any(Date.class))).willReturn(createMockTransactions(7)); // 7개월치 거래내역 생성

        // when: 테스트할 메서드를 실제로 호출합니다.
        UserSavingsDetailResponseDto result = userSavingsService.getSavingsDetail(userId, productId);

        // then: 반환된 결과가 우리의 기대와 일치하는지 검증합니다.
        assertNotNull(result);
        assertEquals("적금", result.getProductType());

        // 1. 최종 달성률 검증 (만기 12개월, 월 10만원 -> 총 120만원 목표. 현재 7개월치 70만원 납입)
        // 기대값: (700,000 / 1,200,000) * 100 = 58
        assertEquals(58, result.getAchievementRate());

        // ---- 가시적 출력(적금)  -------
        System.out.println("===== 적금 상품 테스트 결과 =====");
        System.out.println("최종 달성률: " + result.getAchievementRate() + "%");
        System.out.println("차트 데이터: " + result.getMonthlyChart());
        System.out.println("마지막 달 누적 자산: " + result.getMonthlyChart().get(4).getAmount());

        // 2. 차트 데이터 검증
        assertNotNull(result.getMonthlyChart());
        assertEquals(5, result.getMonthlyChart().size(), "차트 데이터는 최근 5개월치여야 합니다.");
        assertEquals("7월", result.getMonthlyChart().get(4).getMonth(), "차트의 마지막 데이터는 7월이어야 합니다.");

        // 3. 이자 계산 검증 (가장 마지막 달인 7월의 누적 자산 검증)
        // 6월까지 누적원금 60만원에 대한 7월 이자: 600,000 * (0.03 / 12) = 1500
        // 7월까지 누적 이자 총합 (간략 계산): (0+10+20+30+40+50+60)만 * 0.0025 = 5250
        // 7월까지 누적 원금: 700,000
        // 기대 누적 자산: 700,000 + 5250 = 705,250
        assertEquals(705250L, result.getMonthlyChart().get(4).getAmount(), "이자를 포함한 누적 자산이 정확해야 합니다.");
    }

    @Test
    @DisplayName("예금 상품: 이자를 포함한 최근 5개월 자산 차트와 최종 달성률을 정확히 계산한다")
    void getSavingsDetail_forDepositProduct_success() {
        // given
        Long productId = 2L;
        given(userSavingsMapper.findSavingsDetailByIds(userId, productId)).willReturn(createMockDepositVO(productId));
        given(userSavingsMapper.findMonthlyTransactionSums(anyString(), any(Date.class))).willReturn(createMockTransactions(7));

        // when
        UserSavingsDetailResponseDto result = userSavingsService.getSavingsDetail(userId, productId);

        // then
        assertNotNull(result);
        assertEquals("예금", result.getProductType());

        // 1. 최종 달성률 검증 (최초 원금 100만원, 현재 총 납입액 70만원)
        // 기대값: (700,000 / 1,000,000) * 100 = 70
        assertEquals(70, result.getAchievementRate());

        // ---- 가시적 출력(예금)  -------
        System.out.println("===== 예금 상품 테스트 결과 =====");
        System.out.println("최종 달성률: " + result.getAchievementRate() + "%");
        System.out.println("차트 데이터: " + result.getMonthlyChart());
        System.out.println("마지막 달 누적 자산: " + result.getMonthlyChart().get(4).getAmount());

        // 2. 차트 데이터 검증
        assertEquals(5, result.getMonthlyChart().size());
        assertEquals(705250L, result.getMonthlyChart().get(4).getAmount(), "예금 상품의 누적 자산도 정확해야 합니다.");
    }


    // --- 테스트용 가짜 데이터를 생성하는 헬퍼 메서드 --- //

    private UserSavingsVO createMockSavingsVO(Long productId) {
        return UserSavingsVO.builder()
                .productId(productId)
                .principalAmount(1200000L) // 만기 시 총 원금
                .monthlyPayment(100000L)   // 월 납입액 (적금)
                .startDate(Date.from(LocalDate.now().minusMonths(7).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .maturityDate(Date.from(LocalDate.now().plusMonths(5).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .savingsProduct(createMockSavingsProductVO(productId))
                .build();
    }

    private UserSavingsVO createMockDepositVO(Long productId) {
        return UserSavingsVO.builder()
                .productId(productId)
                .principalAmount(1000000L) // 최초 계약 원금
                .monthlyPayment(0L)        // 월 납입액 0 (예금)
                .startDate(Date.from(LocalDate.now().minusMonths(7).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .maturityDate(Date.from(LocalDate.now().plusMonths(5).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .savingsProduct(createMockSavingsProductVO(productId))
                .build();
    }

    private SavingsProductVO createMockSavingsProductVO(Long productId) {
        return SavingsProductVO.builder()
                .productId(productId)
                .productName("테스트 상품")
                .baseRate(3.0f) // 연 3% 금리
                .build();
    }

    private List<TransactionSummaryDto> createMockTransactions(int months) {
        List<TransactionSummaryDto> transactions = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            LocalDate date = LocalDate.now().minusMonths(months - 1 - i);
            TransactionSummaryDto transaction = new TransactionSummaryDto();
            transaction.setMonth(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            transaction.setTotalSavings(100000L); // 매월 10만원씩 입금했다고 가정
            transactions.add(transaction);
        }
        return transactions;
    }
}
