package com.wonnabe.asset.mapper;

import com.wonnabe.asset.dto.TransactionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetOverviewMapper {

    //총자산 조회
    Long getCurrentTotalBalance(@Param("userId") String userId);

    // summaries_cache에서 지난달 스냅샷 금액 자동 조회
    Long getPrevMonthAssetAuto(@Param("userId") String userId);

    //총자산페이지 - 총자산 카테고리 비율(입출금, 저축, 투자, 보험, 기타)
    List<Map<String, Object>> getAssetCategoryBalances(@Param("userId") String userId);

    //총자산페이지 - 자산 상세 내역
    List<Map<String, Object>> getAssetCategoryDetails(@Param("userId") String userId);


    //  총자산 상세페이지 - 카테고리별 계좌
    List<Map<String, Object>> getAccountsByCategory(@Param("userId") String userId,
                                                    @Param("category") String category);

    // 총자산 상세페이지 -카테고리별 보유계좌 거래 내역
    Map<String, Object> getAccountHeaderById(@Param("userId") String userId,
                                                 @Param("accountId") Long accountId);

    List<TransactionDTO> getTransactionsByAccountId(@Param("userId") String userId,
                                                    @Param("accountId") Long accountId);

}
