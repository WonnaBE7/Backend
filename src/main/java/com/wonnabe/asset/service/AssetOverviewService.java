package com.wonnabe.asset.service;

import com.wonnabe.asset.mapper.AssetOverviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

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

}
