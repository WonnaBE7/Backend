package com.wonnabe.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.DiagnosisHistoryResponse;
import com.wonnabe.user.dto.UpdateWonnabeRequest;
import com.wonnabe.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

//    @Test
//    @DisplayName("워너비 업데이트 - 성공")
//    void updateWonnabe_Success() throws Exception {
//        // Given
//        List<Integer> selectedIds = Arrays.asList(1, 2, 3);
//        UpdateWonnabeRequest request = new UpdateWonnabeRequest();
//        // request.setSelected_wonnabe_ids(selectedIds); // setter가 없다면 reflection 사용하거나 테스트용 생성자 필요
//
//        // When & Then
//        mockMvc.perform(patch("/api/user/mypage/wonnabe")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isSuccess").value(true));
//
//        // Verify
//        verify(userService, times(1)).updateWonnabe(any(CustomUser.class), any(UpdateWonnabeRequest.class));
//    }
//
//    @Test
//    @DisplayName("워너비 업데이트 - 빈 배열")
//    void updateWonnabe_EmptyArray() throws Exception {
//        // Given
//        String requestJson = "{\"selected_wonnabe_ids\": []}";
//
//        // When & Then
//        mockMvc.perform(patch("/api/user/mypage/wonnabe")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isSuccess").value(true));
//
//        verify(userService, times(1)).updateWonnabe(any(CustomUser.class), any(UpdateWonnabeRequest.class));
//    }
//
//    @Test
//    @DisplayName("워너비 업데이트 - 잘못된 JSON 형식")
//    void updateWonnabe_InvalidJson() throws Exception {
//        // Given
//        String invalidJson = "{\"selected_wonnabe_ids\": \"invalid\"}";
//
//        // When & Then
//        mockMvc.perform(patch("/api/user/mypage/wonnabe")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(invalidJson))
//                .andExpect(status().isBadRequest());
//
//        verify(userService, never()).updateWonnabe(any(CustomUser.class), any(UpdateWonnabeRequest.class));
//    }
//
//    @Test
//    @DisplayName("워너비 업데이트 - Content-Type 누락")
//    void updateWonnabe_MissingContentType() throws Exception {
//        // Given
//        String requestJson = "{\"selected_wonnabe_ids\": [1, 2, 3]}";
//
//        // When & Then
//        mockMvc.perform(patch("/api/user/mypage/wonnabe")
//                        .content(requestJson))
//                .andExpect(status().isUnsupportedMediaType());
//
//        verify(userService, never()).updateWonnabe(any(CustomUser.class), any(UpdateWonnabeRequest.class));
//    }
//
//    @Test
//    @DisplayName("진단 히스토리 조회 - 성공 (데이터 있음)")
//    void getNowmeHistory_Success_WithData() throws Exception {
//        // Given
//        String userId = "test-user-id";
//        List<DiagnosisHistoryResponse.DiagnosisHistoryItem> historyItems = Arrays.asList(
//                DiagnosisHistoryResponse.DiagnosisHistoryItem.builder()
//                        .diagnosedDate("2025-08-04 21:09:27")
//                        .typeName("가족 중심형")
//                        .score(86)
//                        .build(),
//                DiagnosisHistoryResponse.DiagnosisHistoryItem.builder()
//                        .diagnosedDate("2025-08-03 21:18:24")
//                        .typeName("공격 투자형")
//                        .score(89)
//                        .build()
//        );
//
//        DiagnosisHistoryResponse response = DiagnosisHistoryResponse.builder()
//                .isSuccess(true)
//                .response(historyItems)
//                .build();
//
//        when(userService.getNowmeHistory(userId)).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/user/users/{id}/nowme/history", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isSuccess").value(true))
//                .andExpect(jsonPath("$.response").isArray())
//                .andExpect(jsonPath("$.response.length()").value(2))
//                .andExpect(jsonPath("$.response[0].diagnosedDate").value("2025-08-04 21:09:27"))
//                .andExpect(jsonPath("$.response[0].typeName").value("가족 중심형"))
//                .andExpect(jsonPath("$.response[0].score").value(86))
//                .andExpect(jsonPath("$.response[1].diagnosedDate").value("2025-08-03 21:18:24"))
//                .andExpect(jsonPath("$.response[1].typeName").value("공격 투자형"))
//                .andExpect(jsonPath("$.response[1].score").value(89));
//
//        verify(userService, times(1)).getNowmeHistory(userId);
//    }
//
//    @Test
//    @DisplayName("진단 히스토리 조회 - 성공 (데이터 없음)")
//    void getNowmeHistory_Success_NoData() throws Exception {
//        // Given
//        String userId = "test-user-id";
//        DiagnosisHistoryResponse response = DiagnosisHistoryResponse.builder()
//                .isSuccess(true)
//                .response(Arrays.asList())
//                .build();
//
//        when(userService.getNowmeHistory(userId)).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/user/users/{id}/nowme/history", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isSuccess").value(true))
//                .andExpect(jsonPath("$.response").isArray())
//                .andExpect(jsonPath("$.response.length()").value(0));
//
//        verify(userService, times(1)).getNowmeHistory(userId);
//    }

    @Test
    @DisplayName("진단 히스토리 조회 - 빈 사용자 ID")
    void getNowmeHistory_EmptyUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/user/users/{id}/nowme/history", ""))
                .andExpect(status().isNotFound()); // 빈 ID는 경로 매칭 실패로 404

        verify(userService, never()).getNowmeHistory(any(String.class));
    }

//    @Test
//    @DisplayName("진단 히스토리 조회 - 서비스 예외 발생")
//    void getNowmeHistory_ServiceException() throws Exception {
//        // Given
//        String userId = "test-user-id";
//        when(userService.getNowmeHistory(userId))
//                .thenThrow(new RuntimeException("Database connection failed"));
//
//        // When & Then
//        mockMvc.perform(get("/api/user/users/{id}/nowme/history", userId))
//                .andExpect(status().isInternalServerError());
//
//        verify(userService, times(1)).getNowmeHistory(userId);
//    }
//
//    @Test
//    @DisplayName("진단 히스토리 조회 - 특수문자 포함 사용자 ID")
//    void getNowmeHistory_SpecialCharacterUserId() throws Exception {
//        // Given
//        String userId = "user-123-abc!@#";
//        DiagnosisHistoryResponse response = DiagnosisHistoryResponse.builder()
//                .isSuccess(true)
//                .response(Arrays.asList())
//                .build();
//
//        when(userService.getNowmeHistory(userId)).thenReturn(response);
//
//        // When & Then
//        mockMvc.perform(get("/api/user/users/{id}/nowme/history", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isSuccess").value(true));
//
//        verify(userService, times(1)).getNowmeHistory(userId);
//    }
}