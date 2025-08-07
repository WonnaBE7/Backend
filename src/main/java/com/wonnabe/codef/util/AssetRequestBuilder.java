package com.wonnabe.codef.util;

import com.wonnabe.codef.dto.CodefAuthParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AssetRequestBuilder {

    public Map<String, Object> buildAssetCreateRequest(CodefAuthParam param) {
        Map<String, Object> body = new HashMap<>();

        // ✅ 공통 필수 필드
        body.put("userId", param.getUserId());
        body.put("endpoint", param.getEndpoint());
        body.put("organization", param.getInstitutionCode());
        body.put("connectedId", param.getConnectedId());

        // ✅ 옵션 필드
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
