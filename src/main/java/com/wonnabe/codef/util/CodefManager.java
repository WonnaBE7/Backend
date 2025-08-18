package com.wonnabe.codef.util;

import com.wonnabe.codef.domain.CodefAuth;
import com.wonnabe.codef.mapper.CodefMapper;
import com.wonnabe.codef.service.ConnectedIdService;
import com.wonnabe.codef.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CodefManager {

    private final CodefMapper assetMapper;
    private final AccessTokenService accessTokenService;
    private final ConnectedIdService connectedIdService;
    private static final long SAFETY_MARGIN_SECONDS = 86400; // 하루

    /**
     * 주어진 유저의 모든 기관별 accessToken을 점검하고 필요한 경우 재발급하여 DB 갱신
     * @param userId 사용자 UUID
     */
    public void refreshCodefIfExpired(String userId) {
        List<CodefAuth> authList = assetMapper.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        for (CodefAuth auth : authList) {
            try {
                if (auth.getTokenExpiresAt() == null || auth.getTokenExpiresAt().isBefore(now)) {

                    String newToken = accessTokenService.issueAccessToken();
                    LocalDateTime newExpiresAt = now.plusSeconds(accessTokenService.getExpiresIn() - SAFETY_MARGIN_SECONDS);

                    String connectedId = connectedIdService.issueConnectedId(auth, newToken);

                    assetMapper.updateAccessTokenAndConnectedId(
                            auth.getUserId(),
                            auth.getInstitutionCode(),
                            newToken,
                            newExpiresAt,
                            connectedId
                    );
                }
            } catch (Exception e) {
                System.err.printf("[CODEF 갱신 오류] userId=%s, institution=%s, message=%s%n",
                        auth.getUserId(), auth.getInstitutionCode(), e.getMessage());
            }
        }
    }
}
