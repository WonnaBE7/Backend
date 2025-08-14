package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssetMapper {

    void upsert(@Param("account") UserAccount account);

    String findBankName(@Param("bankCode") String bankCode);

//    List<UserAccount> findAccountsByUserId(@Param("userId") String userId);
//
//    void insertAccounts(@Param("accounts") List<UserAccount> accounts);
//
//    void updateAccount(@Param("account") UserAccount account);
//
//    void saveOrUpdate(@Param("userId") String userId, @Param("accounts") List<UserAccount> accounts);

}
