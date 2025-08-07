//package com.wonnabe.codef.service;
//
//import com.wonnabe.codef.client.CodefApiClient;
//import com.wonnabe.codef.dto.CodefApiParam;
//import com.wonnabe.codef.mapper.CodefMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CodefApiService {
//
//    private final CodefMapper  codefMapper;
//    private final AccessTokenService  accessTokenService;
//    private final CodefApiClient codefApiClient;
//    private final AccessTokenService tokenService;
//
//    public void syncAllApisForUser(String userId) {
//        List<CodefApiParam> apiParams = codefMapper.getApiParamsByUserId(userId);
//        String accessToken = tokenService.issueAccessToken();
//
//        apiParams.parallelStream().forEach(param -> {
//            Map<String, Object> body = buildRequestBody(param);
//            try {
//                String endpoint = param.getEndpoint();
//                codefApiClient.sendGenericRequest(endpoint, accessToken, body);
//                log.info("✅ [{}] 호출 성공", param.getInquiryPurpose());
//            } catch (Exception e) {
//                log.error("❌ [{}] 호출 실패: {}", param.getInquiryPurpose(), e.getMessage());
//            }
//        });
//    }
//
//    private Map<String, Object> buildRequestBody(CodefApiParam param) {
//        Map<String, Object> body = new HashMap<>();
//        if (param.getInstitutionCode() != null) body.put("organization", param.getInstitutionCode());
//        if (param.getBirthDate() != null) body.put("birthDate", param.getBirthDate());
//        if (param.getAccount() != null) body.put("account", param.getAccount());
//        if (param.getAccountPassword() != null) body.put("accountPassword", param.getAccountPassword());
//        if (param.getCardNo() != null) body.put("cardNo", param.getCardNo());
//        if (param.getCardPassword() != null) body.put("cardPassword", param.getCardPassword());
//        if (param.getCardName() != null) body.put("cardName", param.getCardName());
//        if (param.getDuplicateCardIdx() != null) body.put("duplicateCardIdx", param.getDuplicateCardIdx());
//        if (param.getStartDate() != null) body.put("startDate", param.getStartDate());
//        if (param.getEndDate() != null) body.put("endDate", param.getEndDate());
//        if (param.getOrderBy() != null) body.put("orderBy", param.getOrderBy());
//        if (param.getInquiryType() != null) body.put("inquiryType", param.getInquiryType());
//        if (param.getMemberStoreInfoType() != null) body.put("memberStoreInfoType", param.getMemberStoreInfoType());
//        if (param.getWithdrawAccountNo() != null) body.put("withdrawAccountNo", param.getWithdrawAccountNo());
//        if (param.getWithdrawAccountPassword() != null) body.put("withdrawAccountPassword", param.getWithdrawAccountPassword());
//        if (param.getSubAccountId() != null) body.put("id", param.getSubAccountId());
//        if (param.getSubAccountPassword() != null) body.put("addPassword", param.getSubAccountPassword());
//        return body;
//    }
//}
