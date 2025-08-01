package com.wonnabe.asset.service;

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

    public Map<String, Object> getAssetOverview(String userId) {
        Long totalBalance = assetOverviewMapper.getCurrentTotalBalance(userId);
        Long lastMonthBalance = assetOverviewMapper.getLastMonthBalance(userId);

        long current = totalBalance != null ? totalBalance : 0;
        long previous = lastMonthBalance != null ? lastMonthBalance : 0;

        long changeAmount = current - previous;
        double changeRate = (previous == 0) ? 0.0 :
                BigDecimal.valueOf((double) changeAmount / previous * 100)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAmount", current);
        result.put("changeRate", changeRate);
        result.put("changeAmount", changeAmount);
        return result;
    }

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
            case "예적금", "저축" -> "savings";
            case "투자", "증권" -> "investment";
            case "보험" -> "insurance";
            case "연금" -> "pension";
            case "기타" -> "other";
            default -> throw new IllegalArgumentException("유효하지 않은 자산 카테고리입니다: " + category);
        };
    }

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

    public Map<String, Object> getAccountDetailByCategory(String userId, String inputCategory) {
        String dbCategory = mapToDbCategory(inputCategory);

        List<Map<String, Object>> rawAccounts = assetOverviewMapper.getAccountsByCategory(userId, dbCategory);

        List<Map<String, Object>> accounts = rawAccounts.stream()
                .map(account -> {
                    Map<String, Object> m = new LinkedHashMap<>();
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
            case "savings" -> "저축";
            case "investment" -> "투자";
            case "insurance" -> "보험";
            case "pension" -> "연금";
            case "other" -> "기타";
            default -> throw new IllegalArgumentException("유효하지 않은 자산 카테고리입니다: " + input);
        };
    }


}
