package com.wonnabe.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모든 API의 공통 응답 형식
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDto<T> {

    private int code;       // HTTP 상태 코드
    private String message; // 응답 메시지
    private T data;         // 실제 데이터

    /**
     * 성공 응답 생성 팩토리 메서드
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return ApiResponseDto.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 실패 응답 생성 팩토리 메서드
     */
    public static <T> ApiResponseDto<T> error(int code, String message) {
        return ApiResponseDto.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}