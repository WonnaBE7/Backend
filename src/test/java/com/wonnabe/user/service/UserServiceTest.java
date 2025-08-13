package com.wonnabe.user.service;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UpdateWonnabeRequest;
import com.wonnabe.user.dto.UserDetailRequest;
import com.wonnabe.user.dto.UserDetailResponse;
import com.wonnabe.user.mapper.UserMapper;
import com.wonnabe.common.security.account.domain.UserVO;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userMapper, passwordEncoder);

    private CustomUser createFakeUser() {
        UserVO fakeUserVO = new UserVO() {
            @Override
            public String getUserId() {
                return "user123";
            }

            @Override
            public String getEmail() {
                return "test@email.com";
            }

            @Override
            public String getPasswordHash() {
                return "encodedPw";
            }
        };

        return new CustomUser(fakeUserVO);
    }

    @Test
    void getUserInfo() {
        CustomUser mockUser = createFakeUser();

        when(userMapper.selectUserInfo("user123")).thenReturn(null);
        when(userMapper.selectFinancialTendencyNames("user123")).thenReturn(List.of());

        assertDoesNotThrow(() -> userService.getUserInfo(mockUser));
    }

    @Test
    void updateUserInfo() {
        CustomUser mockUser = createFakeUser();
        UpdateUserRequest req = new UpdateUserRequest("홍길동", "pw123");

        when(passwordEncoder.encode("pw123")).thenReturn("encoded");

        assertDoesNotThrow(() -> userService.updateUserInfo(mockUser, req));
    }

    @Test
    void updateWonnabe() {
        CustomUser mockUser = createFakeUser();
        UpdateWonnabeRequest req = new UpdateWonnabeRequest(List.of(1, 2, 3));

        assertDoesNotThrow(() -> userService.updateWonnabe(mockUser, req));
    }

    @Test
    void getNowmeHistory() {
        when(userMapper.selectDiagnosisHistoryLast12Months("user123"))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> userService.getNowmeHistory("user123"));
    }

    @Test
    void getUserDetail() {
        CustomUser mockUser = createFakeUser();

        UserDetailResponse.UserDetailData mockData = UserDetailResponse.UserDetailData.builder()
                .userId("user123")
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

        when(userMapper.selectUserDetail("user123")).thenReturn(mockData);

        assertDoesNotThrow(() -> userService.getUserDetail("user123"));
    }

    @Test
    void createUserDetail() {
        UserDetailRequest request = UserDetailRequest.builder()
                .userId("user123")
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

        when(userMapper.checkUserDetailExists("user123")).thenReturn(0);

        assertDoesNotThrow(() -> userService.createUserDetail(request));
    }

    @Test
    void updateUserDetail() {
        UserDetailRequest request = UserDetailRequest.builder()
                .userId("user123")
                .lifestyleSmoking(1)
                .householdSize(3)
                .build();

        when(userMapper.checkUserDetailExists("user123")).thenReturn(1);

        assertDoesNotThrow(() -> userService.updateUserDetail(request));
    }
}