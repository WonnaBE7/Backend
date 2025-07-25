package com.wonnabe.auth.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {

    int existsByEmail(@Param("email") String email);

    void insertUserProfile(@Param("userId") String userId,
                           @Param("name") String name,
                           @Param("email") String email,
                           @Param("password") String password,
                           @Param("signupType") String signupType);
}
