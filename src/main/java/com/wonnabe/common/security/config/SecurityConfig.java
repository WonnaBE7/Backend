package com.wonnabe.common.security.config;

import com.wonnabe.common.security.filter.AuthenticationErrorFilter;
import com.wonnabe.common.security.filter.JwtAuthenticationFilter;
import com.wonnabe.common.security.filter.JwtUsernamePasswordAuthenticationFilter;
import com.wonnabe.common.security.handler.CustomAccessDeniedHandler;
import com.wonnabe.common.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@Log4j2
@MapperScan(basePackages = {"com.wonnabe.common.security.account.mapper"})
@ComponentScan(basePackages = {"com.wonnabe.common.security"})
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final AuthenticationErrorFilter authenticationErrorFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Spring Security의 AuthenticationManager를 Bean으로 등록합니다.
     *
     * @return AuthenticationManager 객체
     * @throws Exception 예외 발생 시
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean을 등록합니다.
     * BCrypt 알고리즘을 사용합니다.
     *
     * @return BCryptPasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 사용자 인증 처리를 위한 AuthenticationManagerBuilder 구성
     * - UserDetailsService와 PasswordEncoder를 설정합니다.
     *
     * @param auth AuthenticationManagerBuilder
     * @throws Exception 예외 발생 시
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 위한 CorsFilter Bean을 등록합니다.
     * - 모든 origin, header, method 허용
     *
     * @return CorsFilter 객체
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 정적 리소스 및 Swagger 관련 URL을 보안 필터링에서 제외합니다.
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/assets/**",
                "/*",
                "/api/member/**",
                "/swagger-ui.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v2/api-docs"
        );
    }

    /**
     * 문자 인코딩을 UTF-8로 강제하는 필터를 생성합니다.
     *
     * @return CharacterEncodingFilter 객체
     */
    public CharacterEncodingFilter encodingFilter() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        return encodingFilter;
    }

    /**
     * 전체 보안 설정을 구성합니다.
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {

        // 한글 인코딩 필터 설정
        http
            .cors()
            .and()
            .addFilterBefore(encodingFilter(), CsrfFilter.class)
            .addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class) // JWT 예외 감지 후 JSON 에러 응답
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Authorization 헤더에 access token이 있으면 자동으로 검증 & 사용자 인증을 처리
            .addFilterBefore(jwtUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 로그인 시도 처리

        // 인증 및 권한 실패 시 처리 핸들러 설정
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);

        // 인가 정책 설정
        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers("/api/auth/signup").permitAll()

                .antMatchers("/api/auth/login").permitAll()
                .antMatchers("/api/auth/refresh").permitAll()
                .antMatchers("/api/auth/logout").permitAll()
                .antMatchers("/api/auth/kakao/login-url").permitAll()
                .antMatchers("/api/auth/kakao/callback").permitAll()
                .antMatchers("/api/auth/kakao/login").permitAll()
                .antMatchers( "/api/nowme/diagnosis").authenticated()
                .antMatchers("/api/user/**").authenticated()
                .anyRequest().authenticated();

        http.httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}