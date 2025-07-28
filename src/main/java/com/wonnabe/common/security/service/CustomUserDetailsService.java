// com.wonnabe.common.security.service.CustomUserDetailsService.java
package com.wonnabe.common.security.service;

import com.wonnabe.common.security.account.domain.UserVO;
import com.wonnabe.common.security.account.mapper.UserDetailsMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDetailsMapper userDetailsMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserVO user = userDetailsMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email + " not found");
        }
        return new CustomUser(user);
    }

    public UserDetails loadUserByUserUUID(String userUUID) throws UsernameNotFoundException {
        UserVO user = userDetailsMapper.findByUserUUID(userUUID); // UserUUID 기준으로 조회
        if (user == null) {
            throw new UsernameNotFoundException("User not found with userUUID: " + userUUID);
        }
        return new CustomUser(user);
    }

}
