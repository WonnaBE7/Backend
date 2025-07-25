package com.wonnabe.auth.controller;

import com.wonnabe.auth.dto.SignupDTO;
import com.wonnabe.auth.service.AuthService;
import com.wonnabe.common.util.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDTO signupDTO) {
        boolean result = authService.registerUser(signupDTO);
        return result
                ? JsonResponse.ok("회원가입 성공")
                : JsonResponse.error(HttpStatus.CONFLICT, "이미 가입된 이메일입니다");
    }
}