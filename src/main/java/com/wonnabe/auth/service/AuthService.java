package com.wonnabe.auth.service;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.mapper.AuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registerUser(SignupDTO dto) {
        if (authMapper.existsByEmail(dto.getEmail()) > 0) {
            return false; // 이메일 중복
        }
        String userId = UUID.randomUUID().toString();
        String hashedPw = passwordEncoder.encode(dto.getPassword());
        authMapper.insertUserProfile(userId, dto.getName(), dto.getEmail(), hashedPw, "email");
        return true;
    }
}