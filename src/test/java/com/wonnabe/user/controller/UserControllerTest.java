package com.wonnabe.user.controller;

import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UserDetailRequest;
import com.wonnabe.user.service.UserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UserController userController = new UserController(userService);

    @Test
    void getMyInfo() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            when(userService.getUserInfo(mockUser)).thenReturn(null);

            userController.getMyInfo(mockUser);
        });
    }

    @Test
    void updateMyInfo() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            UpdateUserRequest req = new UpdateUserRequest();
            userController.updateMyInfo(mockUser, req);
        });
    }

    @Test
    void updateWonnabe() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            var req = mock(com.wonnabe.user.dto.UpdateWonnabeRequest.class);
            userController.updateWonnabe(mockUser, req);
        });
    }

    @Test
    void getNowmeHistory() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            userController.getNowmeHistory("dummy-user-id", mockUser);
        });
    }

    @Test
    void getUserDetail() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            userController.getUserDetail(mockUser);
        });
    }

    @Test
    void createUserDetail() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            UserDetailRequest req = UserDetailRequest.builder()
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
            userController.createUserDetail(mockUser, req);
        });
    }

    @Test
    void updateUserDetail() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            UserDetailRequest req = UserDetailRequest.builder()
                    .lifestyleSmoking(1)
                    .householdSize(3)
                    .build();
            userController.updateUserDetail(mockUser, req);
        });
    }
}