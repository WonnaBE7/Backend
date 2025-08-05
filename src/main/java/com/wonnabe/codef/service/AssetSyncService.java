package com.wonnabe.codef.service;

import com.wonnabe.codef.domain.UserCard;
import com.wonnabe.codef.domain.UserSaving;
import com.wonnabe.codef.domain.UserTransactions;
import com.wonnabe.codef.dto.AccountListResponse;
import com.wonnabe.codef.dto.CardListWrapper;
import com.wonnabe.codef.dto.CodefTransactionResponse;
import com.wonnabe.codef.dto.TransactionListResponse;
import com.wonnabe.codef.dto.CodefAuthParam;
import com.wonnabe.codef.mapper.*;
import com.wonnabe.codef.client.CodefApiClient;
import com.wonnabe.codef.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetSyncService {

    private final CodefApiClient codefApiClient;

    private final CodefMapper codefMapper;

    private final AssetMapper assetMapper;
    private final SavingsMapper savingsMapper;
    private final AssetCardMapper assetCardMapper;
    private  final UserTransactionsMapper  userTransactionsMapper;
    private final AccountMapper accountMapper;

    /**
     * 유저의 모든 기관에 대해 자산 조회 API를 병렬 호출하여 DB에 반영하는 메서드
     *
     * @param userId 사용자 UUID
     */
    public void syncAllAssets(String userId) {
        List<CodefAuthParam> apiParams = codefMapper.getApiParamsByUserId(userId);

        for (CodefAuthParam param : apiParams) {
            try {

                Object response = codefApiClient.fetchRawAccountResponse(param);

                List<UserAccount> allAccounts = new ArrayList<>();
                List<UserCard> allCards = new ArrayList<>();
                List<UserTransactions> allTransactions = new ArrayList<>();

                if (response instanceof AccountListResponse wrapper) {
                    allAccounts.addAll(wrapper.toUserAccountsFromDeposit(userId, param.getInstitutionCode()));
                    allAccounts.addAll(wrapper.toUserAccountsFromInsurance(userId, param.getInstitutionCode()));
                } else if (response instanceof CardListWrapper cardResponse) {
                    allCards.add(cardResponse.toUserCard(userId));
                } else if (response instanceof CodefTransactionResponse txWrapper) {
                    TransactionListResponse txResponse = txWrapper.getData();
                    allTransactions.addAll(txResponse.toUserTransactions(userId, param.getInstitutionCode(), accountMapper));
                } else {
                    log.warn("⚠️ 알 수 없는 응답 형식: {}, 기관: {}", response.getClass().getSimpleName(), param.getInstitutionCode());
                    continue;
                }

                // ✅ 1. 일반 계좌 처리 (입출금/예적금/보험)
                for (UserAccount account : allAccounts) {
                    String depositCode = account.getAccountDeposit();

                    switch (depositCode) {
                        case "11": // 수시입출
                            assetMapper.upsert(account);
                            break;
                        case "12": // 적금/예금
                            UserSaving savings = convertToUserSavings(account);
                            if (savings.getProductId() != null) savingsMapper.upsert(savings);
                            break;
                        case "50": // 보험
//                            UserInsurance insurance = convertToUserInsurance(account);
//                            insuranceMapper.upsert(insurance);
//                            break;
                        default:
                            assetMapper.upsert(account); // 기본은 입출금
                    }
                }

                // ✅ 2. 카드 계좌 처리
                for (UserCard card : allCards) {
                    String normalized = normalizeCardName(card.getCardName());
                    Long productId = assetCardMapper.findProductIdByKeyword(normalized);
                    card.setProductId(productId);
                    assetCardMapper.upsert(card);
                }

                // 계좌 수시입출 거래내역 처리
                if (!allTransactions.isEmpty()) {
                    userTransactionsMapper.upsertBatch(allTransactions);
                }

                log.info("✅ 자산 동기화 성공 - userId: {}, 기관: {}", userId, param.getInstitutionCode());

            } catch (Exception e) {
                log.error("❌ 자산 동기화 실패 - userId: {}, 기관: {}, 에러: {}", userId, param.getInstitutionCode(), e.getMessage());
            }
        }

//        List<CompletableFuture<Void>> futures = apiParams.stream()
//                .map(param -> CompletableFuture.runAsync(() -> {
//                    try {
//                        // ✅ ① accessToken, connectedId 가져오기
//                        CodefAuthEntity auth = codefMapper.getAuthByUserAndInstitution(param.getUserId(), param.getInstitutionCode());
//                        if (auth == null || auth.getAccessToken() == null || auth.getConnectedId() == null) {
//                            log.warn("⛔ CODEF 인증정보 없음 - userId: {}, 기관: {}", param.getUserId(), param.getInstitutionCode());
//                            return;
//                        }
//
//                        // ✅ ② accessToken, connectedId 수동 주입
//                        param.setAccessToken(auth.getAccessToken());
//                        param.setConnectedId(auth.getConnectedId());
//
//                        // ✅ 범용 API 호출
//                        List<UserAccount> accounts = codefApiClient.fetchGenericAccountList(param);
//
//                        // ✅ 결과 저장 또는 갱신
//                        for (UserAccount account : accounts) {
//                            assetMapper.upsert(account);
//                        }
//                        log.info("✅ 자산 동기화 성공 - userId: {}, 기관: {}", userId, param.getInstitutionCode());
//
//                    } catch (Exception e) {
//                        log.error("❌ 자산 동기화 실패 - userId: {}, 기관: {}, 에러: {}", userId, param.getInstitutionCode(), e.getMessage());
//                    }
//                }))
//                .toList();
//
//        // ✅ 병렬 작업 완료 대기
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private UserSaving convertToUserSavings(UserAccount account) {
        UserSaving savings = new UserSaving();
        savings.setUserId(account.getUserId());

        if (account.getAccountBalance() != null) {
            savings.setPrincipalAmount(BigDecimal.valueOf(account.getAccountBalance()));
        }

        savings.setStartDate(parseDate(account.getAccountStartDate()));
        savings.setMaturityDate(parseDate(account.getAccountEndDate()));

        // ✅ Mapper로 product_id 조회
        Long productId = savingsMapper.findProductIdByKeyword(account.getAccountName().trim());
        savings.setProductId(productId);

        if (productId == null) {
            log.warn("❗ product_id 조회 실패: accountName='{}'", account.getAccountName());
        }
        return savings;
    }

    private Date parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
            return new SimpleDateFormat("yyyyMMdd").parse(yyyymmdd);
        } catch (ParseException e) {
            return null;
        }
    }

    private String normalizeCardName(String rawCardName) {
        if (rawCardName == null) return "";

        return rawCardName
//                .toLowerCase()                 // 소문자 통일
                .replaceAll("\\s+", "")        // 공백 제거
//                .replaceAll("[^가-힣a-zA-Z0-9]", "") // 괄호, 특수기호 제거
                .replace("nori", "노리");       // 수동 매핑 예시
    }

}
