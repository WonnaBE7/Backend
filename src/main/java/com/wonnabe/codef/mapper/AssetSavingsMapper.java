package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserSaving;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetSavingsMapper {

    void upsert(UserSaving savings);

    Long findProductIdByKeyword(@Param("keyword") String keyword);

    Long findSavingIdByUserIdAndProductId(@Param("resAccount") String resAccount);

    Integer findProductIdByAccountId(@Param("accountId") Long accountId);

//    List<UserSavings> findByUserId(@Param("userId") String userId);
//
//    void insert(@Param("savings") UserSavings savings);
//
//    void update(@Param("savings") UserSavings savings);
//
//    void deleteById(@Param("id") Long id);
}
