package com.wonnabe.user.dto;

import lombok.Builder;
import lombok.Getter;


import java.util.List;

@Getter
public class DiagnosisHistoryResponse {
    private final boolean isSuccess;
    private final List<DiagnosisHistoryItem> response;

    @Builder
    public DiagnosisHistoryResponse(boolean isSuccess, List<DiagnosisHistoryItem> response) {
        this.isSuccess = isSuccess;
        this.response = response;
    }

    @Getter
    public static class DiagnosisHistoryItem {
        private final String diagnosedDate;
        private final String typeName;
        private final int score;

        public DiagnosisHistoryItem() {
            this.diagnosedDate = null;
            this.typeName = null;
            this.score = 0;
        }

        @Builder
        public DiagnosisHistoryItem(String diagnosedDate, String typeName, int score) {
            this.diagnosedDate = diagnosedDate;
            this.typeName = typeName;
            this.score = score;
        }
    }
}