package com.wonnabe.common.security.filter;

import com.wonnabe.common.util.JsonResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationErrorFilter extends OncePerRequestFilter {

    /**
     * 모든 요청에 대해 실행되며, JWT 처리 중 발생할 수 있는 예외를 감지하여
     * 클라이언트에게 JSON 형식의 오류 응답을 전송합니다.
     * 이 필터는 JwtAuthenticationFilter보다 앞단에 위치해야 정상적으로 예외를 포착할 수 있습니다.
     *
     * @param request     클라이언트의 HTTP 요청
     * @param response    서버의 HTTP 응답
     * @param filterChain 다음 필터로 요청을 전달하는 체인
     * @throws ServletException 필터 처리 중 발생한 예외
     * @throws IOException      입출력 처리 중 발생한 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            super.doFilter(request, response, filterChain);
        } catch (ExpiredJwtException e) {
            JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "토큰의 유효시간이 지났습니다.");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (ServletException e) {
            JsonResponse.sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
