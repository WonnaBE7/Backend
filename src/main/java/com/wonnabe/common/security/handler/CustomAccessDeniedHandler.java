package com.wonnabe.common.security.handler;

import com.wonnabe.common.util.JsonResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 인가(Authorization) 실패 시 호출되는 메서드입니다.
     * 인증된 사용자가 접근 권한이 없는 자원에 접근하려 할 때 실행됩니다.
     * 예: 일반 사용자가 관리자 전용 API에 접근 시
     *
     * - 로그를 통해 인가 에러를 기록하고,
     * - JsonResponse 유틸을 사용하여 클라이언트에게 403 FORBIDDEN 에러 응답을 반환합니다.
     *
     * @param request                클라이언트의 HTTP 요청 객체
     * @param response               서버의 HTTP 응답 객체
     * @param accessDeniedException 접근 거부 시 발생한 예외 객체
     * @throws IOException           응답 처리 중 발생할 수 있는 입출력 예외
     * @throws ServletException      서블릿 처리 중 발생할 수 있는 예외
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("========== 인가 에러 ============");
        JsonResponse.sendError(response, HttpStatus.FORBIDDEN, "권한이 부족합니다.");
    }
}


