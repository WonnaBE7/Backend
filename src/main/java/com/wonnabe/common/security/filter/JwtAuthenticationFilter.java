package com.wonnabe.common.security.filter;

import com.wonnabe.common.security.service.CustomUserDetailsService;
import com.wonnabe.common.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer "; // 끝에 공백 있음
    private final JwtProcessor jwtProcessor;
    private final UserDetailsService userDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * 전달받은 JWT 토큰을 기반으로 사용자 정보를 조회하고
     * Spring Security의 Authentication 객체를 생성합니다.
     *
     * @param token JWT 토큰 문자열
     * @return UsernamePasswordAuthenticationToken (사용자 인증 정보 포함)
     */
    private Authentication getAuthentication(String token) {
        String UUID = jwtProcessor.getUserIdFromToken(token);
        UserDetails princiapl = customUserDetailsService.loadUserByUserUUID(UUID);
        return new UsernamePasswordAuthenticationToken(princiapl, null, princiapl.getAuthorities());
    }

    /**
     * 요청마다 실행되는 JWT 인증 필터입니다.
     * Authorization 헤더에 포함된 JWT Access Token을 검사하고,
     * 유효한 경우 SecurityContext에 인증 정보를 등록합니다.
     *
     * @param request     클라이언트의 HTTP 요청
     * @param response    서버의 HTTP 응답
     * @param filterChain 다음 필터로 요청을 전달하는 체인
     * @throws ServletException 필터 처리 중 예외 발생 시
     * @throws IOException      입출력 처리 중 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            // 토큰에서 사용자 정보 추출 및 Authentication 객체 구성 후 SecurityContext에 저장
            Authentication authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        super.doFilter(request, response, filterChain);
    }
}
