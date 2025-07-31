package com.wonnabe.asset.mapper;

import com.wonnabe.asset.domain.CodefAuthEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AssetMapper {
    List<CodefAuthEntity> findByUserId(@Param("userId") String userId);

    void updateAccessToken(@Param("userId") String userId,
                           @Param("institutionCode") String institutionCode,
                           @Param("accessToken") String accessToken,
                           @Param("expiresAt") LocalDateTime expiresAt);
}
