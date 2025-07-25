package com.wonnabe.common.security.account.mapper;

import com.wonnabe.common.security.account.domain.MemberVO;

public interface UserDetailsMapper {

    public MemberVO get(String username);

}
