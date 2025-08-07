package com.wonnabe.common.exception;

import com.wonnabe.common.util.JsonResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.core.annotation.Order;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(2)
@Log4j2
public class ApiExceptionAdvice {

    // @Valid 어노테이션으로 인한 유효성 검사 실패 시 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 에러 메시지를 하나로 합칩니다.
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return JsonResponse.error(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // 400 Bad Request - 잘못된 요청 값 처리
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error(e);
        return JsonResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 404 Not Found - 리소스 없음
    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException e) {
        log.error(e);
        return JsonResponse.error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 500 Internal Server Error - 서버 내부 오류
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception e) {
        log.error(e);
        return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}