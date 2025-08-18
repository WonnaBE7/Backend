package com.wonnabe.nowme.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 페르소나 정보 도메인 객체
 * Financial_Tendency_Type 테이블 조회 결과를 담는 객체
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PersonaInfo {

    private String name;                // 페르소나명
    private String description;         // 상세 설명
    private String simpleDescription;   // 간단한 설명
    private String characteristics;     // 특징
    private String iconUrl;            // 아이콘 URL
}