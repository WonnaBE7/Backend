package com.wonnabe.nowme.mapper;

import com.wonnabe.common.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@DisplayName("NowMeMapper 테스트")
@Transactional
@Log4j2
class NowMeMapperTest {

    @Autowired
    private NowMeMapper nowMeMapper;

    private final String TEST_USER_ID = "02747659-2dd1-41d6-80a7-131b9ddfac97"; // 실제 존재하는 사용자 ID

    @Test
    @DisplayName("총 지출 조회 - 성공")
    void getTotalSpending_Success() {
        // When
        double result = nowMeMapper.getTotalSpending(TEST_USER_ID);

        // Then
        assertThat(result, greaterThanOrEqualTo(0.0));
        log.info("Total spending for user {}: {}", TEST_USER_ID, result);
    }

    @Test
    @DisplayName("카테고리별 지출 조회 - 성공")
    void getSpendingByCategories_Success() {
        // Given
        Set<String> categories = Set.of("식비", "교통비");

        // When
        double result = nowMeMapper.getSpendingByCategories(TEST_USER_ID, categories);

        // Then
        assertThat(result, greaterThanOrEqualTo(0.0));
        log.info("Spending by categories {} for user {}: {}", categories, TEST_USER_ID, result);
    }

    @Test
    @DisplayName("가구원 수 조회 - 성공")
    void getHouseholdSize_Success() {
        // When
        int result = nowMeMapper.getHouseholdSize(TEST_USER_ID);

        // Then
        assertThat(result, greaterThan(0));
        log.info("Household size for user {}: {}", TEST_USER_ID, result);
    }

    @Test
    @DisplayName("연간 소득 조회 - 성공")
    void getAnnualIncome_Success() {
        // When
        double result = nowMeMapper.getAnnualIncome(TEST_USER_ID);

        // Then
        assertThat(result, greaterThanOrEqualTo(0.0));
        log.info("Annual income for user {}: {}", TEST_USER_ID, result);
    }

    @Test
    @DisplayName("진단 히스토리 삽입 - 성공")
    void insertDiagnosisHistory_Success() {
        // Given
        Integer nowmeId = 1;
        Double similarity = 0.95;
        String userVector = "[0.1,0.2,0.3,0.4]";

        // When & Then
        assertDoesNotThrow(() ->
                nowMeMapper.insertDiagnosisHistory(TEST_USER_ID, nowmeId, similarity, userVector)
        );
        log.info("Inserted diagnosis history for user {}", TEST_USER_ID);
    }
}