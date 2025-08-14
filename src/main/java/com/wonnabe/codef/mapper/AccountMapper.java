package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {

    /**
     * user_id와 account_number를 기준으로 계좌의 고유 ID(account_id)를 조회합니다.
     *
     * @param userId        사용자 UUID
     * @param accountNumber 계좌 번호
     * @return 해당 계좌의 PK ID (없으면 null)
     */
    Long findAccountIdByUserIdAndAccountNumber(@Param("userId") String userId,
                                               @Param("accountNumber") String accountNumber);

    String findCategoryByAccountId(@Param("accountId") Long accountId);

}
