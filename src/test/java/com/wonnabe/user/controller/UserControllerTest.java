package com.wonnabe.user.controller;

import com.wonnabe.user.dto.UpdateUserRequest;
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

            // when
            userController.getMyInfo(mockUser);

            // then: 예외없이 통과
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
            var req = mock(com.wonnabe.user.dto.UserDetailRequest.class);
            userController.createUserDetail(mockUser, req);
        });
    }

    @Test
    void updateUserDetail() {
        assertDoesNotThrow(() -> {
            var mockUser = mock(com.wonnabe.common.security.account.domain.CustomUser.class);
            var req = mock(com.wonnabe.user.dto.UserDetailRequest.class);
            userController.updateUserDetail(mockUser, req);
        });
    }
}