package com.wonnabe.controller;

import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.NumberFormat;
import java.util.Locale;

@Controller
public class HomeController {

    private final SummariesCacheMapper summariesCacheMapper;

    public HomeController(SummariesCacheMapper summariesCacheMapper) {
        this.summariesCacheMapper = summariesCacheMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        // 테스트용으로 하드코딩된 값 (실제 userId와 yearMonth 넣어서 확인)
        String userId = "111e2222-aaaa-bbbb-cccc-123456789000";  // DB에 맞게 수정
        String currentMonth = "2025-07";
        String lastMonth = "2025-06";

        // 이번 달과 지난 달 소비 금액
        double totalCurrent = summariesCacheMapper.getMonthlyTotalConsumption(userId, currentMonth);
        double totalLast = summariesCacheMapper.getMonthlyTotalConsumption(userId, lastMonth);

        // 차이 계산
        double diff = totalCurrent - totalLast;

        // 숫자 포맷 (3,385,466 형식으로 표시)
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        String formattedCurrent = nf.format(totalCurrent);
        String formattedLast = nf.format(totalLast);
        String formattedDiff = (diff >= 0 ? "+" : "-") + nf.format(Math.abs(diff));

        // 모델에 값 추가
        model.addAttribute("totalCurrent", formattedCurrent);
        model.addAttribute("totalLast", formattedLast);
        model.addAttribute("diff", formattedDiff);
        model.addAttribute("yearMonth", currentMonth);
        model.addAttribute("calculatedUntil", "2025-07-16"); // 임의로 지정, DB에서 가져오려면 추가 쿼리 필요

        return "test";  // test.jsp로 렌더링
    }
}
