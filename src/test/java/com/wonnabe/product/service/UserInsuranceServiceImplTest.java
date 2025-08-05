package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
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

import static org.junit.jupiter.api.Assertions.*;
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
        InsuranceProductVO mockProduct = new InsuranceProductVO();
        mockProduct.setProviderName("테스트보험사");
        mockProduct.setProductName("든든 건강보험");

        mockUserInsurance = new UserInsuranceVO();
        mockUserInsurance.setId(1L);
        mockUserInsurance.setUserId("user123");
        mockUserInsurance.setProductId(3001L); // productId 설정
        mockUserInsurance.setMonthlyPremium(new BigDecimal("100000"));
        mockUserInsurance.setStartDate(java.sql.Date.valueOf(java.time.LocalDate.now().minusMonths(6))); // 시작일 설정
        mockUserInsurance.setProduct(mockProduct);
    }

    @Test
    @DisplayName("보험 상세 정보 조회 시, DTO로 정확하게 변환되어야 한다.")
    void getDetailByProductId_should_return_dto_when_data_exists() {
        // given: Mock Mapper가 mockUserInsurance 객체를 반환하도록 설정
        String userId = "user123";
        Long productId = 1L;
        when(userInsuranceMapper.findDetailByProductId(userId, productId)).thenReturn(mockUserInsurance);

        // when: 서비스 메소드 호출
        UserInsuranceDetailDTO responseDTO = userInsuranceService.getDetailByProductId(userId, productId);

        // then: 결과 검증
        assertNotNull(responseDTO, "응답 DTO는 null이 아니어야 합니다.");
        assertEquals("든든 건강보험", responseDTO.getProductName(), "상품명이 일치해야 합니다.");
        assertEquals(0, new BigDecimal("100000").compareTo(responseDTO.getMonthlyPremium()), "월 납입액이 일치해야 합니다.");
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
