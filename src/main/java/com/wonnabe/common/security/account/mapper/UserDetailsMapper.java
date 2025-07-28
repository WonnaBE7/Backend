// com.wonnabe.common.security.account.mapper.UserDetailsMapper.java
package com.wonnabe.common.security.account.mapper;

import com.wonnabe.common.security.account.domain.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDetailsMapper {
    UserVO findByEmail(String email);

    UserVO findByUserUUID(@Param("userUUID") String userUUID);

}
