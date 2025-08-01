package com.wonnabe.nowme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NowMeResponseDTO {

    private boolean success;          // 저장 성공 여부
    private String personaName;       // 예: "자린고비형"

    public static NowMeResponseDTO success(String personaName) {
        return new NowMeResponseDTO(true, personaName);
    }

    public static NowMeResponseDTO failure() {
        return new NowMeResponseDTO(false, null);
    }
}