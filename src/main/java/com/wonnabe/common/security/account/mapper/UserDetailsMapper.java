package com.wonnabe.common.security.account.mapper;

import com.wonnabe.common.security.account.domain.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 사용자 인증 및 조회를 위한 MyBatis 매퍼 인터페이스입니다.
 * User_Profile 테이블에서 사용자 정보를 조회합니다.
 */
@Mapper
public interface UserDetailsMapper {

    /**
     * 이메일(email)을 기준으로 사용자 정보를 조회합니다.
     * 로그인 시 사용됩니다.
     *
     * @param email 사용자 이메일
     * @return UserVO 사용자 정보 객체 (없으면 null 반환)
     */
    UserVO findByEmail(String email);


    /**
     * 사용자 고유 ID(UUID)를 기준으로 사용자 정보를 조회합니다.
     * JWT 인증 후 사용자 정보를 불러올 때 사용됩니다.
     *
     * @param userUUID 사용자 UUID
     * @return UserVO 사용자 정보 객체 (없으면 null 반환)
     */
    UserVO findByUserUUID(@Param("userUUID") String userUUID);

}
