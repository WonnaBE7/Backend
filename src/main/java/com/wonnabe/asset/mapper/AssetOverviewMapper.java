package com.wonnabe.asset.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetOverviewMapper {

    Long getCurrentTotalBalance(@Param("userId") String userId);

    Long getLastMonthBalance(@Param("userId") String userId);
}
