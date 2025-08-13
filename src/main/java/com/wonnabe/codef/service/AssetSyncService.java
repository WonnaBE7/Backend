package com.wonnabe.codef.service;

import com.wonnabe.codef.domain.*;
import com.wonnabe.codef.dto.*;
import com.wonnabe.codef.mapper.*;
import com.wonnabe.codef.client.CodefApiClient;
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
    private final CodefSavingsMapper savingsMapper;
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
                List<CardTransactions> allCardTransactions = new ArrayList<>();

                if (response instanceof AccountListResponse wrapper) {
                    // 보유 계좌 리스트 조회
                    allAccounts.addAll(wrapper.toUserAccountsFromDeposit(userId, param.getInstitutionCode()));
                }
                else if (response instanceof AccountTransactionListWrapper atlWrapper) {
                    // 입출금 계좌 거래내역 리스트 조회
                    AccountTransactionListResponse txResponse = atlWrapper.getData();
                    allTransactions.addAll(txResponse.toUserTransactions(userId, param.getInstitutionCode(), accountMapper));
                }
                else if (response instanceof SavingTransactionListResponse savingTxWrapper) {
                    // 적금 계좌 거래내역 리스트 조회
                    List<UserTransactions> savingTxs = savingTxWrapper.toUserTransactions(userId, param.getInstitutionCode(), accountMapper, savingsMapper);
                    allTransactions.addAll(savingTxs);
                }
                else if (response instanceof CardListWrapper cardResponse) {
                    // 보유 카드 리스트 조회
                    allCards.addAll(cardResponse.toUserCards(userId));
                }
                else if (response instanceof CardTransactionListWrapper crlWrapper) {
                    // 카드 거래내역 리스트 조회
                    List<CardTransactionData> cardTransactionDataList = crlWrapper.getData();
                    CardApprovalListResponse cardApprovalListResponse = new CardApprovalListResponse();
                    cardApprovalListResponse.setData(cardTransactionDataList); // 데이터를 세팅

                    allCardTransactions.addAll(
                            cardApprovalListResponse.toCardTransactions(userId, param.getInstitutionCode(), assetCardMapper)
                    );
                }
                else if (response instanceof InvestAccountListWrapper investWrapper) {
                    allAccounts.addAll(
                            investWrapper.toUserAccountsFromSecurities(userId, param.getInstitutionCode())
                    );
                }
                else {
                    log.warn("⚠️ 알 수 없는 응답 형식: {}, 기관: {}", response.getClass().getSimpleName(), param.getInstitutionCode());
                    continue;
                }

                // ✅ 1. 일반 계좌 처리 (입출금/예적금/보험)
                for (UserAccount account : allAccounts) {

                    // 카테고리 선셋팅(증권)은 존중
                    if (account.getCategory() != null && !account.getCategory().isBlank()) {
                        String bankName = assetMapper.findBankName(account.getInstitutionCode());
                        account.setBankName(bankName);
                        assetMapper.upsert(account);
                        continue;
                    }

                    // 은행 분류는 기존 deposit code 로직 유지
                    String depositCode = account.getAccountDeposit();
                    String bankName = assetMapper.findBankName(account.getInstitutionCode());
                    account.setBankName(bankName);

                    switch (depositCode) {
                        case "11": // 수시입출
                            account.setCategory("입출금");
                            assetMapper.upsert(account);
                            break;
                        case "14":
                            account.setCategory("연금");
                            assetMapper.upsert(account);
                            break;
                        case "12": // 적금/예금
                            UserSaving savings = convertToUserSavings(account);
                            if (savings.getProductId() != null) savingsMapper.upsert(savings);
                            break;
                        default:
                            account.setCategory("기타");
                            assetMapper.upsert(account); // 기본은 입출금
                    }
                }

                // ✅ 2. 카드 계좌 처리
                for (UserCard card : allCards) {
                    String normalized = normalizeCardName(card.getCardName());
                    Long productId = assetCardMapper.findProductIdByKeyword(normalized, param.getInstitutionCode());
                    card.setProductId(productId);
                    assetCardMapper.upsert(card);
                }

                // ✅ 거래내역 처리 (입출금/적금)
                if (!allTransactions.isEmpty()) {
                    userTransactionsMapper.upsertBatch(allTransactions);
                }

                // ✅ 카드 거래내역
                if (!allCardTransactions.isEmpty()) {
                    assetCardMapper.upsertBatch(allCardTransactions);
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
        savings.setAccountNumber(account.getAccountNumber());

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
                .replaceAll("\\s+", "")        // 공백 제거
                .replaceAll("[\\s()\\[\\]{}]", "")  // 공백, 괄호류 제거
                .replace("nori", "노리")
                .replace("VIVAePlatinum", "하나");
    }

}
