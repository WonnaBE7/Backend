package com.wonnabe.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class JsonResponse {

    private static final ObjectMapper om = new ObjectMapper();

    /**
     * 메시지만 포함된 성공 응답을 반환합니다.
     *
     * @param message 응답 메시지
     * @return ResponseEntity 객체 (code, message)
     */
    public static ResponseEntity<Object> ok(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("message", message);
        return ResponseEntity.ok(body);
    }

    /**
     * 메시지와 데이터가 포함된 성공 응답을 반환합니다.
     *
     * @param message 응답 메시지
     * @param data    응답 데이터 객체
     * @return ResponseEntity 객체 (code, message, data)
     */
    public static ResponseEntity<Object> ok(String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 200);
        body.put("message", message);
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    /**
     * 에러 응답 (모든 에러는 이 메서드로 통일 처리)
     *
     * @param status  HTTP 상태 코드 (예: 400, 401, 409, 500 등)
     * @param message 에러 메시지
     * @return 일관된 JSON 포맷의 에러 응답
     */
    public static ResponseEntity<Object> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * HttpServletResponse로 JSON 에러 응답을 직접 전송합니다.
     *
     * @param response HttpServletResponse
     * @param status   HTTP 상태 코드
     * @param message  에러 메시지
     */
    public static void sendError(HttpServletResponse response, HttpStatus status, String message) {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("code", status.value());
        body.put("message", message);

        try (Writer writer = response.getWriter()) {
            om.writeValue(writer, body);
        } catch (IOException e) {
            e.printStackTrace(); // 혹은 log 처리
        }
    }

    /** 202 Accepted (메시지만) */
    public static ResponseEntity<Object> accepted(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.ACCEPTED.value());
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    /** 202 Accepted (메시지 + 데이터) */
    public static ResponseEntity<Object> accepted(String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.ACCEPTED.value());
        body.put("message", message);
        body.put("data", data);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

}
