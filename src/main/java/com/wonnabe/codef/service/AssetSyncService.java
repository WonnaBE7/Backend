package com.wonnabe.codef.service;

import com.wonnabe.codef.domain.*;
import com.wonnabe.codef.dto.auth.CodefAuthParam;
import com.wonnabe.codef.dto.bank.account.BankAccountListResponse;
import com.wonnabe.codef.dto.bank.transaction.BankAccountTransactionPayload;
import com.wonnabe.codef.dto.bank.transaction.BankAccountTransactionListResponse;
import com.wonnabe.codef.dto.bank.savings.BankSavingsTransactionListResponse;
import com.wonnabe.codef.dto.card.account.CardListResponse;
import com.wonnabe.codef.dto.card.transaction.CardTransactionListResponse;
import com.wonnabe.codef.dto.invest.account.InvestAccountListResponse;
import com.wonnabe.codef.mapper.*;
import com.wonnabe.codef.client.CodefApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetSyncService {

    private final CodefApiClient codefApiClient;
    private final CodefMapper codefMapper;
    private final AssetMapper assetMapper;
    private final AssetSavingsMapper savingsMapper;
    private final AssetCardMapper assetCardMapper;
    private  final UserTransactionsMapper  userTransactionsMapper;
    private final AccountMapper accountMapper;
    private final ThreadPoolTaskExecutor assetSyncExecutor;

    /**
     * 사용자의 모든 기관에 대해 CODEF 자산 API를 병렬로 호출하고 결과를 DB에 반영합니다.
     * - Codef_API에서 활성화된 엔드포인트 목록을 조회합니다.
     * - 각 기관별로 별도 작업으로 offload하여 병렬 처리합니다(동시성은 assetSyncExecutor로 제한).
     * - 모든 병렬 작업이 완료될 때까지 대기한 뒤 종료합니다.
     *
     * @param userId 동기화 대상 사용자 ID (UUID)
     */
    public void syncAllAssets(String userId) {
        List<CodefAuthParam> apiParams = codefMapper.getApiParamsByUserId(userId);
        if (apiParams.isEmpty()) {
            log.info("동기화 대상 없음 - userId: {}", userId);
            return;
        }
        // 병렬 호출 + 제한 동시성 (스레드풀로 제한)
        List<CompletableFuture<Void>> futures = apiParams.stream()
                .map(param -> CompletableFuture.runAsync(() -> syncOneInstitution(userId, param), assetSyncExecutor))
                .toList();

        // 백그라운드 작업이므로 여기서 기다려도 OK (완료 기준으로 로그/마킹 등 처리)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /** 기존 syncAllAssets 대신, 기관별 작업을 스레드풀에 제출만 하고 즉시 리턴 */
    public void enqueueAllInstitutions(String userId) {
        List<CodefAuthParam> apiParams = codefMapper.getApiParamsByUserId(userId);
        if (apiParams.isEmpty()) {
            log.info("동기화 대상 없음 - userId={}", userId);
            return;
        }
        apiParams.forEach(param ->
                assetSyncExecutor.execute(() -> syncOneInstitution(userId, param)) // ⬅️ no join
        );
    }

    /**
     * 단일 기관에 대한 자산 동기화를 수행합니다.
     * - CODEF API를 호출하여 기관별 응답을 수신합니다.
     * - 응답 타입(Account/Transaction/Card 등)에 따라 도메인 객체로 변환합니다.
     * - 계좌/카드/거래내역을 각각 upsert(또는 배치 upsert)로 DB에 반영합니다.
     * - 처리 과정에서 발생하는 예외는 캐치하여 로그로 남깁니다.
     *
     * @param userId 동기화 대상 사용자 ID (UUID)
     * @param param  기관별 호출 파라미터(엔드포인트, 토큰, 기간, 옵션 등)
     */
    private void syncOneInstitution(String userId, CodefAuthParam param) {

        try {
            Object response = codefApiClient.fetchRawAccountResponse(param);

            List<UserAccount> allAccounts = new ArrayList<>();
            List<UserCard> allCards = new ArrayList<>();
            List<UserTransaction> allTransactions = new ArrayList<>();
            List<CardTransaction> allCardTransactions = new ArrayList<>();

            if (response instanceof BankAccountListResponse wrapper) {
                // 보유 계좌 리스트 조회
                allAccounts.addAll(wrapper.toUserAccountsFromDeposit(userId, param.getInstitutionCode()));
            }
            else if (response instanceof BankAccountTransactionListResponse atlWrapper) {
                // 입출금 계좌 거래내역 리스트 조회
                BankAccountTransactionPayload txResponse = atlWrapper.getData();
                allTransactions.addAll(txResponse.toUserTransactions(userId, param.getInstitutionCode(), accountMapper));
            }
            else if (response instanceof BankSavingsTransactionListResponse savingTxWrapper) {
                // 적금 계좌 거래내역 리스트 조회
                List<UserTransaction> savingTxs = savingTxWrapper.toUserTransactions(userId, param.getInstitutionCode(), savingsMapper);
                allTransactions.addAll(savingTxs);
            }
            else if (response instanceof CardListResponse cardResponse) {
                // 보유 카드 리스트 조회
                allCards.addAll(cardResponse.toUserCards(userId));
            }
            else if (response instanceof CardTransactionListResponse crlWrapper) {
                // 카드 거래내역 리스트 조회
                allCardTransactions.addAll(crlWrapper.toCardTransactions(userId, param.getInstitutionCode(), assetCardMapper));
            }
            else if (response instanceof InvestAccountListResponse investWrapper) {
                // 보유 증권 계좌 리스트 조회
                allAccounts.addAll(investWrapper.toUserAccountsFromSecurities(userId, param.getInstitutionCode()));
            }
            else {
                log.warn("⚠️ 알 수 없는 응답 형식: {}, 기관: {}", response.getClass().getSimpleName(), param.getInstitutionCode());
                return;
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

                String depositCode = account.getAccountDeposit();
                String bankName = assetMapper.findBankName(account.getInstitutionCode());
                account.setBankName(bankName);
                switch (depositCode) {
                    case "11":
                        account.setCategory("입출금");
                        assetMapper.upsert(account);
                        break;
                    case "14":
                        account.setCategory("연금");
                        assetMapper.upsert(account);
                        break;
                    case "12":
                        UserSaving savings = convertToUserSavings(account);
                        if (savings.getProductId() != null) savingsMapper.upsert(savings);
                        break;
                    default:
                        account.setCategory("기타");
                        assetMapper.upsert(account);
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

    /**
     * 수신한 계좌 정보(UserAccount)를 예·적금 도메인(UserSaving)으로 변환합니다.
     * - 잔액을 원금(principalAmount)으로 매핑합니다.
     * - 시작일/만기일을 Date로 파싱하여 설정합니다.
     * - 상품명 키워드로 productId를 조회하여 설정합니다.
     *
     * @param account 예·적금 유형의 계좌 정보
     * @return 변환된 UserSaving 객체 (productId가 없을 수 있음)
     */
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

    /**
     * 문자열(yyyyMMdd)을 java.util.Date로 변환합니다.
     * - 입력이 null 또는 공백이면 null을 반환합니다.
     * - 파싱 오류 발생 시 null을 반환합니다.
     *
     * @param yyyymmdd "yyyyMMdd" 형식의 날짜 문자열
     * @return 파싱된 Date 객체 또는 null
     */
    private Date parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
            return new SimpleDateFormat("yyyyMMdd").parse(yyyymmdd);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 카드 상품명 문자열을 검색/매핑에 유리하도록 정규화합니다.
     * - 공백 및 괄호류 제거
     * - 일부 영문 표기 교정(nori → 노리, VIVAePlatinum → 하나)
     *
     * @param rawCardName 원본 카드명
     * @return 정규화된 카드명 문자열(Null 입력 시 빈 문자열)
     */
    private String normalizeCardName(String rawCardName) {
        if (rawCardName == null) return "";

        return rawCardName
                .replaceAll("\\s+", "")        // 공백 제거
                .replaceAll("[\\s()\\[\\]{}]", "")  // 공백, 괄호류 제거
                .replace("nori", "노리")
                .replace("VIVAePlatinum", "하나");
    }

}
