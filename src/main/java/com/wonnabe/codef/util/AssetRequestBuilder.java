package com.wonnabe.codef.util;

import com.wonnabe.codef.dto.auth.CodefAuthParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AssetRequestBuilder {

    /**
     * CODEF 자산 조회/생성 계열 API에 전달할 요청 바디를 생성합니다.
     * 공통 필수 필드(userId, endpoint, organization, connectedId)를 포함하고,
     * 선택 필드(account, startDate, endDate, orderBy, inquiryType, memberStoreInfoType)는
     * 값이 존재할 때만 바디에 추가합니다.
     *
     * @param param CODEF 호출 파라미터(사용자/기관/계좌/조회기간/정렬/조회유형 등)
     * @return CODEF 자산 API에 전달할 요청 바디 맵
     */
    public Map<String, Object> buildAssetCreateRequest(CodefAuthParam param) {
        Map<String, Object> body = new HashMap<>();

        body.put("userId", param.getUserId());
        body.put("endpoint", param.getEndpoint());
        body.put("organization", param.getInstitutionCode());
        body.put("connectedId", param.getConnectedId());

        if (param.getAccount() != null && !param.getAccount().isBlank()) {
            body.put("account", param.getAccount());
        }
        if (param.getStartDate() != null && !param.getStartDate().isBlank()) {
            body.put("startDate", param.getStartDate());
        }
        if (param.getEndDate() != null && !param.getEndDate().isBlank()) {
            body.put("endDate", param.getEndDate());
        }
        if (param.getOrderBy() != null && !param.getOrderBy().isBlank()) {
            body.put("orderBy", param.getOrderBy());
        }
        if (param.getInquiryType() != null && !param.getInquiryType().isBlank()) {
            body.put("inquiryType", param.getInquiryType());
        }
        if (param.getMemberStoreInfoType() != null && !param.getMemberStoreInfoType().isBlank()) {
            body.put("memberStoreInfoType", param.getMemberStoreInfoType());
        }

        return body;
    }
}
