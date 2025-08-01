package com.wonnabe.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 사용자 이름 및 비밀번호를 업데이트합니다.
     * - 사용자 ID를 기준으로 DB에서 해당 사용자의 정보를 찾아 수정합니다.
     * - 이름(name)과 비밀번호(passwordHash)를 모두 수정합니다.
     *
     * @param userId       수정 대상 사용자의 UUID
     * @param name         수정할 사용자 이름
     * @param passwordHash 수정할 사용자 비밀번호 (BCrypt 등으로 암호화된 값)
     */
    void updateUser(@Param("userId") String userId,
                    @Param("name") String name,
                    @Param("passwordHash") String passwordHash);
}
