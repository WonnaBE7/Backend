package com.wonnabe.controller;

import com.wonnabe.asset.mapper.SummariesCacheMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        String yearMonth = "2025-07";

        double total = summariesCacheMapper.getMonthlyTotalConsumption(userId, yearMonth);
        model.addAttribute("totalConsumption", total);

        return "test"; // test.jsp 렌더링
    }
}
