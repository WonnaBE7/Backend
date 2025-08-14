package com.wonnabe.asset.service;

import com.wonnabe.asset.dto.TransactionDTO;
import com.wonnabe.asset.mapper.AssetOverviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetOverviewService {

    private final AssetOverviewMapper assetOverviewMapper;

    //메인페이지 - 총자산 현황
    public Map<String, Object> getAssetOverview(String userId) {
        Long totalBalance = assetOverviewMapper.getCurrentTotalBalance(userId);
        Long prevAsset    = assetOverviewMapper.getPrevMonthAssetAuto(userId);

        long current  = totalBalance != null ? totalBalance : 0L;
        long previous = prevAsset    != null ? prevAsset    : 0L;

        long changeAmount = current - previous;
        double changeRate = (previous == 0)
                ? 0.0
                : java.math.BigDecimal.valueOf(changeAmount)
                .divide(java.math.BigDecimal.valueOf(previous), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .setScale(1, java.math.RoundingMode.HALF_UP)
                .doubleValue();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("totalAmount", current);
        result.put("changeAmount", changeAmount);
        result.put("changeRate",  changeRate);
        return result;
    }

    //총자산페이지 - 총자산 카테고리 비율(입출금, 저축, 투자, 보험, 기타)
    public Map<String, Object> getAssetCategoryRatio(String userId) {
        List<Map<String, Object>> raw = assetOverviewMapper.getAssetCategoryBalances(userId);

        // 1. 변환 및 그룹핑
        Map<String, Long> grouped = raw.stream()
                .collect(Collectors.groupingBy(
                        row -> mapToKey((String) row.get("category")),
                        Collectors.summingLong(row -> ((Number) row.get("balance")).longValue())
                ));

        long total = grouped.values().stream().mapToLong(Long::longValue).sum();

        // 2. 고정된 카테고리 순서
        List<String> orderedKeys = List.of("checking", "savings", "investment", "insurance", "pension", "other");

        // 3. 순서에 맞게 정렬된 결과 구성
        List<Map<String, Object>> result = orderedKeys.stream()
                .filter(grouped::containsKey)
                .map(key -> {
                    long balance = grouped.get(key);
                    double ratio = total > 0 ? (balance * 100.0 / total) : 0;

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("assetCategory", key);
                    map.put("percentage", Math.round(ratio * 10) / 10.0);
                    return map;
                })
                .collect(Collectors.toList());

        return Map.of("categories", result);
    }

    private String mapToKey(String category) {
        return switch (category) {
            case "입출금" -> "checking";
            case "예적금"-> "savings";
            case "투자" -> "investment";
            case "보험" -> "insurance";
            case "연금" -> "pension";
            case "기타" -> "other";
            default -> throw new IllegalArgumentException("유효하지 않은 자산 카테고리입니다: " + category);
        };
    }

    //총자산페이지 - 자산 상세 내역
    public Map<String, Object> getAssetCategoryDetails(String userId) {
        List<Map<String, Object>> raw = assetOverviewMapper.getAssetCategoryDetails(userId);

        // 고정된 순서로 정렬
        List<String> orderedKeys = List.of("checking", "savings", "investment", "insurance", "pension", "other");

        // 변환 및 그룹핑
        Map<String, List<Map<String, Object>>> grouped = raw.stream()
                .collect(Collectors.groupingBy(
                        row -> mapToKey((String) row.get("category"))
                ));

        List<Map<String, Object>> categories = orderedKeys.stream()
                .filter(grouped::containsKey)
                .map(key -> {
                    long sum = grouped.get(key).stream()
                            .mapToLong(r -> ((Number) r.get("amount")).longValue())
                            .sum();
                    int count = grouped.get(key).stream()
                            .mapToInt(r -> ((Number) r.get("accountsCount")).intValue())
                            .sum();

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("assetCategory", key);
                    map.put("amount", sum);
                    map.put("accountsCount", count);
                    return map;
                })
                .collect(Collectors.toList());

        return Map.of("categories", categories);
    }

    //총자산 상세페이지 - 카테고리별 계좌
    public Map<String, Object> getAccountDetailByCategory(String userId, String inputCategory) {
        String dbCategory = mapToDbCategory(inputCategory);

        List<Map<String, Object>> rawAccounts = assetOverviewMapper.getAccountsByCategory(userId, dbCategory);

        List<Map<String, Object>> accounts = rawAccounts.stream()
                .map(account -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("accountId", account.get("accountId"));
                    m.put("bankName", account.get("bankName"));
                    m.put("accountName", account.get("accountName"));
                    m.put("accountNumber", account.get("accountNumber"));
                    m.put("balance", account.get("balance"));
                    return m;
                }).collect(Collectors.toList());

        long total = accounts.stream()
                .mapToLong(a -> ((Number) a.get("balance")).longValue())
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assetCategory", inputCategory);
        result.put("totalAmount", total);
        result.put("accounts", accounts);
        return result;
    }


    // 영어 → 한글 매핑
    private String mapToDbCategory(String input) {
        return switch (input) {
            case "checking" -> "입출금";
            case "savings" -> "예적금";
            case "investment" -> "투자";
            case "insurance" -> "보험";
            case "pension" -> "연금";
            case "other" -> "기타";
            default -> throw new IllegalArgumentException("유효하지 않은 자산 카테고리입니다: " + input);
        };
    }

    // 총자산 상세페이지 -카테고리별 보유계좌 거래 내역
    // 총자산 상세페이지 - 카테고리별 보유계좌 거래 내역
    public Map<String, Object> getAccountTransactionsById(String userId, Long accountId) {
        // 1) 헤더
        Map<String, Object> header = assetOverviewMapper.getAccountHeaderById(userId, accountId);
        if (header == null || header.isEmpty()) {
            throw new IllegalArgumentException("해당 계좌가 없거나 권한이 없습니다: " + accountId);
        }

        // 2) 거래내역 (DTO로 받기)
        List<TransactionDTO> transactions = assetOverviewMapper.getTransactionsByAccountId(userId, accountId);
        if (transactions == null) transactions = java.util.Collections.emptyList();

        // accountName 제외한 응답용 뷰
        List<Map<String, Object>> txView = transactions.stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("transactionName", t.getTransactionName());
                    m.put("transactionDate", t.getTransactionDate());
                    m.put("transactionTime", t.getTransactionTime());
                    m.put("amount", t.getAmount());
                    return m;
                })
                .collect(Collectors.toList());

        // 3) 계좌번호 마스킹(하이픈/공백 유지)
        String masked = maskAccountNumberKeepHyphen((String) header.get("accountNumber"));

        // 4) 최종 응답 (변수명 result 권장)
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bankName", header.get("bankName"));
        result.put("accountNumber", masked);
        result.put("transactions", txView);
        return result;
    }

    private String maskAccountNumberKeepHyphen(String s) {
        if (s == null) return null;
        int totalDigits = 0;
        for (int i = 0; i < s.length(); i++) if (Character.isDigit(s.charAt(i))) totalDigits++;

        int seen = 0;
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                seen++;
                // 앞 4자리와 끝 2자리는 노출, 그 사이 숫자만 마스크
                if (seen > 4 && seen <= totalDigits - 2) out.append('*');
                else out.append(c);
            } else {
                out.append(c); // 하이픈/공백 등 비숫자는 그대로 유지
            }
        }
        return out.toString();
    }


}
