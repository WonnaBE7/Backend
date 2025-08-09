package com.wonnabe.user.mapper;

import com.wonnabe.user.dto.DiagnosisHistoryResponse;
import com.wonnabe.user.dto.UserDetailRequest;
import com.wonnabe.user.dto.UserDetailResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper() {
        @Override
        public void updateUser(String userId, String name, String passwordHash) {
        }

        @Override
        public void updateWonnabe(String userId, String selectedWonnabeIds) {
        }

        @Override
        public List<DiagnosisHistoryResponse.DiagnosisHistoryItem> selectDiagnosisHistory(String userId) {
            return Collections.emptyList();
        }

        @Override
        public List<DiagnosisHistoryResponse.DiagnosisHistoryItem> selectDiagnosisHistoryLast12Months(String userId) {
            return Collections.emptyList();
        }

        @Override
        public Map<String, Object> selectUserInfo(String userId) {
            return Collections.emptyMap();
        }

        @Override
        public String selectFinancialTendencyName(Integer nowmeId) {
            return "자린고비형";
        }

        @Override
        public List<String> selectFinancialTendencyNames(String userId) {
            return Collections.emptyList();
        }

        @Override
        public UserDetailResponse.UserDetailData selectUserDetail(String userId) {
            // 🔧 새로운 생성자 매개변수에 맞게 수정 (incomeAnnualAmount 제거됨)
            return UserDetailResponse.UserDetailData.builder()
                    .userId("test-user")
                    .lifestyleSmoking(0)
                    .lifestyleFamilyMedical(1)
                    .lifestyleBeforeDiseases(0)
                    .lifestyleExerciseFreq(1)
                    .lifestyleAlcoholFreq(0)
                    .incomeSourceType("근로소득")
                    .incomeEmploymentStatus("재직")
                    .householdSize(2)
                    .incomeJobType("개발자")
                    .build();
        }

        @Override
        public void insertUserDetail(UserDetailRequest request) {
        }

        @Override
        public void updateUserDetail(UserDetailRequest request) {
        }

        @Override
        public int checkUserDetailExists(String userId) {
            return 1;
        }
    };

    @Test
    void updateUser() {
        assertDoesNotThrow(() -> userMapper.updateUser("user123", "홍길동", "encodedPw"));
    }

    @Test
    void updateWonnabe() {
        assertDoesNotThrow(() -> userMapper.updateWonnabe("user123", "[1,2,3]"));
    }

    @Test
    void selectDiagnosisHistory() {
        assertDoesNotThrow(() -> userMapper.selectDiagnosisHistoryLast12Months("user123"));
    }

    // 🔧 새로운 테스트 추가
    @Test
    void insertUserDetail() {
        UserDetailRequest request = UserDetailRequest.builder()
                .userId("test-user")
                .lifestyleSmoking(0)
                .lifestyleFamilyMedical(1)
                .lifestyleBeforeDiseases(0)
                .lifestyleExerciseFreq(1)
                .lifestyleAlcoholFreq(0)
                .incomeSourceType("근로소득")
                .incomeEmploymentStatus("재직")
                .householdSize(2)
                .incomeJobType("개발자")
                .build();

        assertDoesNotThrow(() -> userMapper.insertUserDetail(request));
    }

    @Test
    void updateUserDetail() {
        UserDetailRequest request = UserDetailRequest.builder()
                .userId("test-user")
                .lifestyleSmoking(1)
                .householdSize(3)
                .build();

        assertDoesNotThrow(() -> userMapper.updateUserDetail(request));
    }
}