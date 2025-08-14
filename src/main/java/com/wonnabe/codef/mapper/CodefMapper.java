package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.CodefAuth;
import com.wonnabe.codef.dto.auth.CodefAuthParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CodefMapper {

    /**
     * 사용자 ID를 기준으로 CODEF 인증 정보 목록을 조회합니다.
     *
     * @param userId 조회할 사용자 ID(UUID)
     * @return 사용자에 매핑된 CodefAuthEntity 목록(없으면 빈 리스트)
     */
    List<CodefAuth> findByUserId(@Param("userId") String userId);

    /**
     * 사용자·기관별 CODEF 인증 정보의 Access Token, 만료 시각, Connected ID를 갱신합니다.
     *
     * @param userId           사용자 ID(UUID)
     * @param institutionCode  기관 코드
     * @param accessToken      갱신할 Access Token
     * @param expiresAt        토큰 만료 시각
     * @param connectedId      갱신할 Connected ID
     */
    void updateAccessTokenAndConnectedId(@Param("userId") String userId,
                                         @Param("institutionCode") String institutionCode,
                                         @Param("accessToken") String accessToken,
                                         @Param("expiresAt") LocalDateTime expiresAt,
                                         @Param("connectedId") String connectedId);

    /**
     * 사용자 ID를 기준으로 활성화된 API 호출 파라미터 목록을 조회합니다.
     * (Codef_Auth와 Codef_API 조인 결과)
     *
     * @param userId 조회할 사용자 ID(UUID)
     * @return 엔드포인트·토큰·옵션이 포함된 CodefAuthParam 목록(없으면 빈 리스트)
     */
    List<CodefAuthParam> getApiParamsByUserId(@Param("userId") String userId);


    /**
     * 사용자·기관 코드를 기준으로 단일 인증 파라미터를 조회합니다.
     *
     * @param userId          사용자 ID(UUID)
     * @param institutionCode 기관 코드
     * @return CodefAuthParam(없으면 null)
     */
    CodefAuthParam getAuthByUserAndInstitution(
            @Param("userId") String userId,
            @Param("institutionCode") String institutionCode
    );
}
