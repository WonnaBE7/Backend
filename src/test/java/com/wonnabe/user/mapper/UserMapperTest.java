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
            return new UserDetailResponse.UserDetailData();
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
}