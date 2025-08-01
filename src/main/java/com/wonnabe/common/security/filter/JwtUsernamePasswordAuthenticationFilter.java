package com.wonnabe.common.security.filter;

import com.wonnabe.common.security.account.dto.LoginDTO;
import com.wonnabe.common.security.handler.LoginFailureHandler;
import com.wonnabe.common.security.handler.LoginSuccessHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 이메일/비밀번호 기반 로그인 인증 필터입니다.
 * 요청 URL이 "/api/auth/login"일 때 동작하며,
 * 인증 성공/실패 핸들러를 통해 후속 처리를 위임합니다.
 */
@Log4j2
@Component
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * 필터 생성자입니다.
     * 인증 매니저와 인증 성공/실패 핸들러를 설정합니다.
     *
     * @param authenticationManager  인증 처리 매니저
     * @param loginSuccessHandler    인증 성공 시 처리할 핸들러
     * @param loginFailureHandler    인증 실패 시 처리할 핸들러
     */
    public JwtUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/auth/login");
        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
    }

    /**
     * 클라이언트로부터 전달받은 이메일/비밀번호를 기반으로 인증 시도를 수행합니다.
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 인증 객체(Authentication)
     * @throws AuthenticationException 인증 실패 시 예외 발생
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginDTO login = LoginDTO.of(request);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());
        return getAuthenticationManager().authenticate(authenticationToken);
    }
}