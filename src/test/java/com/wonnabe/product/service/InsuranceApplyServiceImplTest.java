package com.wonnabe.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.config.RedisConfig;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.dto.InsuranceApplyRequestDTO;
import com.wonnabe.product.mapper.InsuranceMapper;
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
class InsuranceApplyServiceImplTest {

    @Autowired
    private InsuranceApplyService insuranceApplyService;

    @Autowired
    private InsuranceMapper insuranceMapper; // 검증을 위해 매퍼를 직접 사용

    private final String TEST_USER_ID = "b2c3d4e5-f678-9012-abcd-ef12gh34ij56";
    private final String TEST_PRODUCT_ID = "3002";

    @Test
    @Transactional // 테스트 후 DB 변경사항을 롤백하여 테스트의 독립성을 보장
    @DisplayName("보험 신청 서비스 성공 테스트")
    void applyUserInsurance_success() throws Exception {
        // given: 테스트를 위한 요청 DTO 준비
        InsuranceApplyRequestDTO requestDTO = InsuranceApplyRequestDTO.builder()
                .insuranceId(TEST_PRODUCT_ID)
                .productType("insurance")
                .build();

        // when: 보험 신청 서비스 호출
        insuranceApplyService.applyUserInsurance(requestDTO, TEST_USER_ID);

        // then: 서비스 실행 후 결과가 DB에 올바르게 반영되었는지 검증

        // 1. User_Insurance 테이블에 데이터가 정상적으로 삽입되었는지 확인
        UserInsuranceVO newInsurance = insuranceMapper.findUserInsuranceByProductId(Long.parseLong(TEST_PRODUCT_ID), TEST_USER_ID);
        assertNotNull(newInsurance, "보험 가입 후 User_Insurance 테이블에서 해당 정보를 찾을 수 없습니다.");

        // 2. User_Info 테이블의 my_insurance_ids에 새로운 보험 ID가 추가되었는지 확인
        String myInsuranceIdsJson = insuranceMapper.getMyInsuranceIdsJson(TEST_USER_ID);
        assertNotNull(myInsuranceIdsJson, "사용자의 보험 목록(JSON)이 null입니다.");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Long> myInsuranceIds = objectMapper.readValue(myInsuranceIdsJson, new TypeReference<List<Long>>() {});
        assertTrue(myInsuranceIds.contains(newInsurance.getId()), "사용자 정보에 새로운 보험 ID가 추가되지 않았습니다.");

        System.out.println("성공적으로 가입된 보험 정보: " + newInsurance);
        System.out.println("업데이트된 사용자 보험 목록: " + myInsuranceIdsJson);
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 보험 상품 신청 시 예외 발생 테스트")
    void applyUserInsurance_fail_with_non_existent_product() {
        // given: 존재하지 않는 보험 ID로 요청 DTO 준비
        InsuranceApplyRequestDTO requestDTO = InsuranceApplyRequestDTO.builder()
                .insuranceId("9999") // 존재하지 않는 ID
                .productType("insurance")
                .build();

        // when & then: 서비스를 실행했을 때, NoSuchElementException이 발생하는지 확인
        assertThrows(java.util.NoSuchElementException.class, () -> {
            insuranceApplyService.applyUserInsurance(requestDTO, TEST_USER_ID);
        }, "존재하지 않는 상품에 대해 NoSuchElementException이 발생해야 합니다.");
    }
}
