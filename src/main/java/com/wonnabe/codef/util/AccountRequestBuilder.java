package com.wonnabe.codef.util;

import com.wonnabe.codef.client.CodefClient;
import com.wonnabe.codef.domain.CodefAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountRequestBuilder {

    private final CodefClient codefAuthClient;

    /**
     * CODEF 계정(Connected ID 발급 전) 등록 요청 바디를 생성합니다.
     * 필수 필드(countryCode, businessType, clientType, loginType, organization, id, password)를 채우고,
     * 선택 필드(birthDate, cardPassword)는 값이 있을 때만 포함합니다.
     * 비밀번호류(password, cardPassword)는 {@link CodefClient#encryptPassword(String)}로 RSA 암호화하여 넣습니다.
     *
     * @param auth 계정 등록에 필요한 사용자/기관 인증 정보(아이디, 비밀번호, 기관 코드 등)
     * @return CODEF 계정 등록 API에 전달할 요청 파라미터 맵
     */

    public Map<String, Object> buildAccountCreateRequest(CodefAuth auth) {
        Map<String, Object> params = new HashMap<>();

        params.put("countryCode", auth.getCountryCode());
        params.put("businessType", auth.getBusinessType());
        params.put("clientType", auth.getClientType());
        params.put("loginType", auth.getLoginType());
        params.put("organization", auth.getInstitutionCode());
        params.put("id", auth.getLoginId());

        String encryptedPw = codefAuthClient.encryptPassword(auth.getLoginPassword());
        params.put("password", encryptedPw);

        if (auth.getBirthDate() != null && !auth.getBirthDate().isBlank()) {
            params.put("birthDate", auth.getBirthDate());
        }
        if (auth.getCardPassword() != null && !auth.getCardPassword().isBlank()) {
            String encryptedCardPw = codefAuthClient.encryptPassword(auth.getCardPassword());
            params.put("cardPassword", encryptedCardPw);
        }
        return params;
    }
}
