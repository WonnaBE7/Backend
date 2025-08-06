package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.CodefAuthEntity;
import com.wonnabe.codef.dto.CodefAuthParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CodefMapper {
    List<CodefAuthEntity> findByUserId(@Param("userId") String userId);

    void updateAccessTokenAndConnectedId(@Param("userId") String userId,
                                         @Param("institutionCode") String institutionCode,
                                         @Param("accessToken") String accessToken,
                                         @Param("expiresAt") LocalDateTime expiresAt,
                                         @Param("connectedId") String connectedId);

    List<CodefAuthParam> getApiParamsByUserId(@Param("userId") String userId);

    CodefAuthParam getAuthByUserAndInstitution(
            @Param("userId") String userId,
            @Param("institutionCode") String institutionCode
    );
}
