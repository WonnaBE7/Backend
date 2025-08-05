package com.wonnabe.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User_info 테이블과 대응
 *  추천 로직을 위해 사용되는 테이블
 *  WonnabeID 리스트 반환방식
 * Recommend Savings / Insurance 사용 변수 나눠존재
 * -> 추후 Income이 아니라 UserInfo로 클래스명 수정 필요

 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIncomeInfoVO {
    private String userId;
    private String selectedWonnabeIds;     // "[1,2,3]" 형태의 문자열

    // 예적금 상품 추천을 위한 변수
    private String incomeSourceType;       // 소득원 (급여/사업/프리랜스/기타)
    private String incomeEmploymentStatus; // 고용상태 (정규직/계약직/학생/무직)

    // 보험 상품 추천을 위한 변수
    private String smokingStatus; // 흡연 여부 (Y/N)
    private String familyMedicalHistory; // 가족 병력 (고혈압/당뇨/암 등)
    private String pastMedicalHistory; // 과거 병력 (Y/N)
    private String exerciseFrequency; // 운동 빈도 (매일, 주3회, 주1회, 안함)
    private String drinkingFrequency; // 음주 빈도 (자주, 가끔, 안함)


    // 페르소나 ID 리스트로 변환
    public List<Integer> getPersonaIds() {
        // 예: ["1", "2"] → List<Integer>로 변환
        String ids = selectedWonnabeIds.replaceAll("[\\[\\]\\s\"]", ""); // 대괄호, 공백, 쌍따옴표 제거
        return Arrays.stream(ids.split(","))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
