package com.wonnabe.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.SavingsApplyRequestDTO;
import com.wonnabe.product.mapper.SavingsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@ActiveProfiles("test")
@Transactional // 테스트 후 DB 변경사항을 롤백하여 테스트의 독립성을 보장
class SavingsApplyServiceImplTest {

    @Autowired
    private SavingsApplyService savingsApplyService;

    @Autowired
    private SavingsMapper savingsMapper; // 검증을 위해 매퍼를 직접 사용

    private final String TEST_USER_ID = "b2c3d4e5-f678-9012-abcd-ef12gh34ij56";
    private final long TEST_PRODUCT_ID = 1301L;

    @Test
    @DisplayName("예적금 신청 서비스 성공 테스트")
    void applyUserSavings_success() throws Exception {
        // given: 테스트를 위한 요청 DTO 준비
        SavingsApplyRequestDTO requestDTO = SavingsApplyRequestDTO.builder()
                .productId(TEST_PRODUCT_ID)
                .principalAmount(1000000L)
                .monthlyPayment(100000L)
                .joinPeriod(12)
                .build();

        // when: 예적금 신청 서비스 호출
        savingsApplyService.applyUserSavings(requestDTO, TEST_USER_ID);

        // then: 서비스 실행 후 결과가 DB에 올바르게 반영되었는지 검증

        // 1. User_Savings 테이블에 데이터가 정상적으로 삽입/수정되었는지 확인
        UserSavingsVO appliedSavings = savingsMapper.findUserSavingsByProductId(TEST_PRODUCT_ID, TEST_USER_ID);
        assertNotNull(appliedSavings, "예적금 가입 후 User_Savings 테이블에서 해당 정보를 찾을 수 없습니다.");
        assertEquals(requestDTO.getPrincipalAmount(), appliedSavings.getPrincipalAmount(), "요청한 원금과 DB에 저장된 원금이 일치하지 않습니다.");
        assertEquals(requestDTO.getMonthlyPayment(), appliedSavings.getMonthlyPayment(), "요청한 월 납입액과 DB에 저장된 월 납입액이 일치하지 않습니다.");


        // 2. User_Info 테이블의 my_savings_ids에 새로운 예적금 ID가 추가되었는지 확인
        String mySavingsIdsJson = savingsMapper.getMySavingsIdsJson(TEST_USER_ID);
        assertNotNull(mySavingsIdsJson, "사용자의 예적금 목록(JSON)이 null입니다.");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Long> mySavingsIds = objectMapper.readValue(mySavingsIdsJson, new TypeReference<List<Long>>() {});

        // 검증 로직 수정: PK(id)가 아닌 상품 ID(productId)가 포함되어 있는지 확인해야 합니다.
        assertTrue(mySavingsIds.contains(appliedSavings.getProductId()), "사용자 정보에 새로운 예적금 ID가 추가되지 않았습니다.");

        System.out.println("성공적으로 가입된 예적금 정보: " + appliedSavings);
        System.out.println("업데이트된 사용자 예적금 목록: " + mySavingsIdsJson);
    }

    @Test
    @DisplayName("존재하지 않는 예적금 상품 신청 시 예외 발생 테스트")
    void applyUserSavings_fail_with_non_existent_product() {
        // given: 존재하지 않는 예적금 ID로 요청 DTO 준비
        SavingsApplyRequestDTO requestDTO = SavingsApplyRequestDTO.builder()
                .productId(9999L) // 존재하지 않는 ID
                .principalAmount(1000000L)
                .monthlyPayment(100000L)
                .joinPeriod(12)
                .build();

        // when & then: 서비스를 실행했을 때, NoSuchElementException이 발생하는지 확인
        assertThrows(NoSuchElementException.class, () -> {
            savingsApplyService.applyUserSavings(requestDTO, TEST_USER_ID);
        }, "존재하지 않는 상품에 대해 NoSuchElementException이 발생해야 합니다.");
    }
}