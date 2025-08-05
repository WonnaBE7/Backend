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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@ActiveProfiles("test")
class SavingsApplyServiceImplTest {

    @Autowired
    private SavingsApplyService savingsApplyService;

    @Autowired
    private SavingsMapper savingsMapper; // 검증을 위해 매퍼를 직접 사용

    private final String TEST_USER_ID = "1469a2a3-213d-427e-b29f-f79d58f51190";
    private final long TEST_PRODUCT_ID = 1310L;

    @Test
    @Transactional // 테스트 후 DB 변경사항을 롤백하여 테스트의 독립성을 보장
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

        // 1. User_Savings 테이블에 데이터가 정상적으로 삽입되었는지 확인
        UserSavingsVO newSavings = savingsMapper.findUserSavingsByProductId(TEST_PRODUCT_ID, TEST_USER_ID);
        assertNotNull(newSavings, "예적금 가입 후 User_Savings 테이블에서 해당 정보를 찾을 수 없습니다.");

        // 2. User_Info 테이블의 my_savings_ids에 새로운 예적금 ID가 추가되었는지 확인
        String mySavingsIdsJson = savingsMapper.getMySavingsIdsJson(TEST_USER_ID);
        assertNotNull(mySavingsIdsJson, "사용자의 예적금 목록(JSON)이 null입니다.");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Long> mySavingsIds = objectMapper.readValue(mySavingsIdsJson, new TypeReference<List<Long>>() {});
        assertTrue(mySavingsIds.contains(newSavings.getId()), "사용자 정보에 새로운 예적금 ID가 추가되지 않았습니다.");

        System.out.println("성공적으로 가입된 예적금 정보: " + newSavings);
        System.out.println("업데이트된 사용자 예적금 목록: " + mySavingsIdsJson);
    }

    @Test
    @Transactional
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
        assertThrows(java.util.NoSuchElementException.class, () -> {
            savingsApplyService.applyUserSavings(requestDTO, TEST_USER_ID);
        }, "존재하지 않는 상품에 대해 NoSuchElementException이 발생해야 합니다.");
    }
}
