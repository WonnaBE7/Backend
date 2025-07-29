package com.wonnabe.auth.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {

    // 회원가입 - 이메일 중복 검사
    int existsByEmail(@Param("email") String email);

    // 회원가입 - 회원정보 저장
    void insertUserProfile(@Param("userId") String userId,
                           @Param("name") String name,
                           @Param("email") String email,
                           @Param("password") String password,
                           @Param("signupType") String signupType);

    // 로그인 - 로그인 요청이 오면 비밀번호 비교하고 토큰 발급해야 함

    // 카카오 유저 조회
    User findUserByEmail(@Param("email") String email);

    // 카카오 유저 저장
    void insertKakaoUser(@Param("user") User user);
}
