package com.wonnabe.auth.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {

    /**
     * 이메일을 기준으로 사용자 존재 여부를 확인합니다.
     *
     * @param email 확인할 이메일 주소
     * @return 존재하면 1 이상, 존재하지 않으면 0
     */
    int existsByEmail(@Param("email") String email);

    /**
     * 사용자 정보를 user_profile 테이블에 삽입합니다.
     *
     * @param userId     UUID 형식의 사용자 ID
     * @param name       사용자 이름
     * @param email      사용자 이메일
     * @param password   암호화된 비밀번호
     * @param signupType 가입 유형 (예: "email", "kakao" 등)
     */
    void insertUserProfile(@Param("userId") String userId,
                           @Param("name") String name,
                           @Param("email") String email,
                           @Param("password") String password,
                           @Param("signupType") String signupType);
}
