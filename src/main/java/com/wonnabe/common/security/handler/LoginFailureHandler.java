package com.wonnabe.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 로그인 인증 실패 시 호출되는 메서드입니다.
     * 실패 사유를 로그로 출력하고, 사용자에게 JSON 형식의 에러 응답을 반환합니다.
     *
     * @param request  클라이언트 요청 객체
     * @param response 서버 응답 객체
     * @param exception 인증 실패 시 발생한 예외 객체
     * @throws IOException JSON 직렬화 또는 응답 작성 중 예외 발생 시
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("로그인 실패: {}", exception.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        objectMapper.writeValue(response.getWriter(), body);
    }
}
