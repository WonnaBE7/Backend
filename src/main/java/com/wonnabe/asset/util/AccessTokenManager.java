package com.wonnabe.asset.util;

import com.wonnabe.asset.client.CodefAuthClient;
import com.wonnabe.asset.domain.CodefAuthEntity;
import com.wonnabe.asset.dto.TokenResponseDto;
import com.wonnabe.asset.mapper.AssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccessTokenManager {

    private final CodefAuthClient codefAuthClient;
    private final AssetMapper assetMapper;
    private static final long SAFETY_MARGIN_SECONDS = 86400; // 하루


    /**
     * 주어진 유저의 모든 기관별 accessToken을 점검하고 필요한 경우 재발급하여 DB 갱신
     * @param userId 사용자 UUID
     */
    public void validateAndRefreshTokens(String userId) {
        List<CodefAuthEntity> authList = assetMapper.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        for (CodefAuthEntity auth : authList) {
            boolean expired = auth.getTokenExpiresAt() == null || auth.getTokenExpiresAt().isBefore(now);

            if (expired) {
                TokenResponseDto tokenResponse = codefAuthClient.requestAccessToken();
                String newToken = tokenResponse.getAccessToken();
                LocalDateTime newExpiresAt = now.plusSeconds(Long.parseLong(tokenResponse.getExpiresIn()) - SAFETY_MARGIN_SECONDS);

                assetMapper.updateAccessToken(
                        auth.getUserId(),
                        auth.getInstitutionCode(),
                        newToken,
                        newExpiresAt
                );
            }
        }
    }
}
