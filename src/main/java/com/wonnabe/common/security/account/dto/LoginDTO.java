package com.wonnabe.common.security.account.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletRequest;

/**
 * 로그인 요청 시 사용되는 DTO 클래스입니다.
 * 클라이언트로부터 받은 email, password 값을 역직렬화하여 저장합니다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginDTO {
    private String email;
    private String password;

    /**
     * HttpServletRequest에서 JSON 형태로 전달된 로그인 정보를 파싱하여 LoginDTO로 변환합니다.
     *
     * @param request HTTP 요청 객체 (JSON body에 email, password 포함)
     * @return LoginDTO 객체
     * @throws BadCredentialsException email 또는 password가 없거나 파싱 실패 시 예외 발생
     */

    public static LoginDTO of(HttpServletRequest request) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(request.getInputStream(), LoginDTO.class);
        }catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("email 또는 password가 없습니다.");
        }
    }
}