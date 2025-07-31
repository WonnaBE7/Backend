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
 * Recommend Savings / Insurance 사용 변수
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoVO {
    private String userId;
    private String selectedWonnabeIds;     // "[1,2,3]" 형태의 문자열

    // 예적금 상품 추천을 위한 변수
    private String incomeSourceType;       // 소득원 (급여/사업/프리랜스/기타)
    private String incomeEmploymentStatus; // 고용상태 (정규직/계약직/학생/무직)

    // 보험 상품 추천을 위한 변수



    // 페르소나 ID 리스트로 변환
    public List<Integer> getPersonaIds() {
        // "[1,2,3]" → List<Integer>로 변환
        String ids = selectedWonnabeIds.replaceAll("[\\[\\]\\s]", "");
        return Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
