package com.wonnabe.common.security.service;

import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.common.security.account.mapper.UserDetailsMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security에서 인증 처리를 위한 사용자 정보를 조회하는 서비스입니다.
 * 기본 이메일 기반 조회뿐만 아니라 UUID 기반 조회도 지원합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDetailsMapper userDetailsMapper;

    /**
     * Spring Security에서 기본적으로 사용하는 사용자 조회 메서드입니다.
     * email 기준으로 사용자 정보를 조회하여 CustomUser 객체로 반환합니다.
     *
     * @param email 사용자의 이메일
     * @return UserDetails 객체(CustomUser)
     * @throws UsernameNotFoundException 사용자를 찾지 못한 경우 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserVO user = userDetailsMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email + " not found");
        }
        return new CustomUser(user);
    }


    /**
     * 사용자 고유 식별자(UUID)를 기반으로 사용자 정보를 조회합니다.
     * JWT에서 userUUID로 사용자를 식별할 때 이 메서드를 사용합니다.
     *
     * @param userUUID 사용자 UUID
     * @return UserDetails 객체(CustomUser)
     * @throws UsernameNotFoundException UUID로 사용자를 찾지 못한 경우 예외 발생
     */
    public UserDetails loadUserByUserUUID(String userUUID) throws UsernameNotFoundException {
        UserVO user = userDetailsMapper.findByUserUUID(userUUID); // UserUUID 기준으로 조회
        if (user == null) {
            throw new UsernameNotFoundException("User not found with userUUID: " + userUUID);
        }
        return new CustomUser(user);
    }
}
