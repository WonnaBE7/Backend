package com.wonnabe.codef.util;

import com.wonnabe.codef.client.CodefClient;
import com.wonnabe.codef.domain.CodefAuthEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountRequestBuilder {

    private final CodefClient codefAuthClient;

    public Map<String, Object> buildAccountCreateRequest(CodefAuthEntity auth) {
        Map<String, Object> params = new HashMap<>();

        // ✅ 필수 필드
        params.put("countryCode", auth.getCountryCode());            // KR
        params.put("businessType", auth.getBusinessType());          // BK, CD, ST, IS
        params.put("clientType", auth.getClientType());              // P, B, A
        params.put("loginType", auth.getLoginType());                // 1 (ID/PW)
        params.put("organization", auth.getInstitutionCode());       // ex: 0004
        params.put("id", auth.getLoginId());                         // 사용자 ID

        String encryptedPw = codefAuthClient.encryptPassword(auth.getLoginPassword());
        params.put("password", encryptedPw); // RSA 암호화 후 전달

        // ✅ 옵션 필드: 값이 있을 때만 추가
        if (auth.getBirthDate() != null && !auth.getBirthDate().isBlank()) {
            params.put("birthDate", auth.getBirthDate());            // YYMMDD
        }
        if (auth.getCardPassword() != null && !auth.getCardPassword().isBlank()) {
            String encryptedCardPw = codefAuthClient.encryptPassword(auth.getCardPassword());
            params.put("cardPassword", encryptedCardPw); // RSA 암호화된 카드 비밀번호
        }
        return params;
    }

}
