package com.wonnabe.nowme.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.dto.NowMeResponseDTO;
import com.wonnabe.nowme.service.NowMeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NowMeController 테스트")
class NowMeControllerTest {

    @Mock
    private NowMeService nowMeService;

    @InjectMocks
    private NowMeController nowMeController;

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
    @DisplayName("진단 - 성공")
    void diagnose() {
        // Given
        CustomUser mockUser = createFakeUser();
        NowMeRequestDTO req = new NowMeRequestDTO();
        NowMeResponseDTO mockResponse = mock(NowMeResponseDTO.class);
        when(nowMeService.diagnose("user123", req)).thenReturn(mockResponse);

        // When & Then
        assertDoesNotThrow(() -> nowMeController.diagnose(mockUser, req));
        verify(nowMeService, times(1)).diagnose("user123", req);
    }
}