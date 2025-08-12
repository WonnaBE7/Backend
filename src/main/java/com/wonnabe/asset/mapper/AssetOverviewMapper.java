package com.wonnabe.asset.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetOverviewMapper {

    Long getCurrentTotalBalance(@Param("userId") String userId);

    Long getLastMonthBalance(@Param("userId") String userId);

    List<Map<String, Object>> getAssetCategoryBalances(@Param("userId") String userId);

    List<Map<String, Object>> getAssetCategoryDetails(@Param("userId") String userId);


    List<Map<String, Object>> getAccountsByCategory(@Param("userId") String userId,
                                                    @Param("category") String category);

    // 총자산 상세페이지 -카테고리별 보유계좌 거래 내역
    Map<String, Object> getAccountHeaderByNumber(@Param("userId") String userId,
                                                 @Param("accountNum") String accountNum);

    List<Map<String, Object>> getTransactionsByAccountNumber(@Param("userId") String userId,
                                                             @Param("accountNum") String accountNum);
}
