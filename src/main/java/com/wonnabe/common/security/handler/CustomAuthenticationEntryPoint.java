package com.wonnabe.common.security.handler;

import com.wonnabe.common.util.JsonResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출되는 메서드
     * Spring Security에서 인증 예외(AuthenticationException)가 발생하면 이 entry point가 트리거됩니다.
     * 로그를 출력하고 JsonResponse 유틸을 통해 401 UNAUTHORIZED 상태의 JSON 에러 응답을 반환합니다.
     *
     * @param request       클라이언트의 HTTP 요청 객체
     * @param response      서버의 HTTP 응답 객체
     * @param authException 인증 실패 시 발생한 예외
     * @throws IOException        응답 작성 중 입출력 예외 발생 가능
     * @throws ServletException   서블릿 처리 중 예외 발생 가능
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.error("========== 인증 에러 ============");
        JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, authException.getMessage());
    }
}