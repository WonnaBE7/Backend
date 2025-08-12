package com.wonnabe.nowme.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 진단 이력 저장 객체
 * DB에 저장되는 진단 결과 정보
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisHistory {

    private Long id;                     // PK (자동 생성)
    private Long userId;                 // 진단받은 사용자 ID - 누가
    private String personaName;          // 진단된 페르소나 이름 - 어떤 페르소나로
    private double similarity;           // 최고 유사도 점수 - 얼마나 유사하게
    private LocalDateTime diagnosedAt;   // 진단 받은 시각 - 언제 진단받았는지

    // DB 저장용이 아닌, 새 진단 결과를 생성할 때 사용할 생성자
    public DiagnosisHistory(Long userId, String personaName, double similarity, LocalDateTime diagnosedAt) {
        this.userId = userId;
        this.personaName = personaName;
        this.similarity = similarity;
        this.diagnosedAt = diagnosedAt;
    }

    // 진단 기록을 간편하게 만들기 위한 헬퍼 메서드
    // (DB에 저장할 DiagnosisHistory 객체를 편하게 만들어주는 도우미 메서드)
    public static DiagnosisHistory create(Long userId, String personaName, double similarity) {
        return new DiagnosisHistory(userId, personaName, similarity, LocalDateTime.now());
    }
}