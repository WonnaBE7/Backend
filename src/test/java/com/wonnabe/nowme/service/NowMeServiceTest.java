package com.wonnabe.nowme.service;

import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.dto.NowMeResponseDTO;
import com.wonnabe.nowme.evaluation.ActivityEvaluator;
import com.wonnabe.nowme.evaluation.PlanningEvaluator;
import com.wonnabe.nowme.evaluation.RiskEvaluator;
import com.wonnabe.nowme.evaluation.SpendingEvaluator;
import com.wonnabe.nowme.mapper.NowMeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NowMeService 테스트")
class NowMeServiceTest {

    @Mock private ActivityEvaluator activityEvaluator;
    @Mock private SpendingEvaluator spendingEvaluator;
    @Mock private PlanningEvaluator planningEvaluator;
    @Mock private RiskEvaluator riskEvaluator;
    @Mock private NowMeMapper nowMeMapper;

    @InjectMocks
    private NowMeService nowMeService;

    @Test
    @DisplayName("진단 - 성공")
    void diagnose() {
        // Given
        NowMeRequestDTO req = new NowMeRequestDTO();
        when(activityEvaluator.calculateFinalScore("user123", req)).thenReturn(0.5);
        when(spendingEvaluator.calculateFinalScore("user123", req)).thenReturn(0.5);
        when(planningEvaluator.calculateFinalScore("user123", req)).thenReturn(0.5);
        when(riskEvaluator.calculateFinalScore("user123", req)).thenReturn(0.5);

        // When & Then
        assertDoesNotThrow(() -> nowMeService.diagnose("user123", req));

        verify(activityEvaluator, times(1)).calculateFinalScore("user123", req);
        verify(spendingEvaluator, times(1)).calculateFinalScore("user123", req);
        verify(planningEvaluator, times(1)).calculateFinalScore("user123", req);
        verify(riskEvaluator, times(1)).calculateFinalScore("user123", req);
    }
}