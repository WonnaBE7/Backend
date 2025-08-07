package com.wonnabe.user.service;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UpdateWonnabeRequest;
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
}