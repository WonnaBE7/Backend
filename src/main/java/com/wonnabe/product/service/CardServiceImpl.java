package com.wonnabe.product.service;

import com.wonnabe.product.dto.UserCardDTO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.NoSuchElementException;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;

    // 카드 계약기간을 계산함
    public int calculateTerm(LocalDate issueDate, LocalDate expiryDate) {
        Period period = Period.between(issueDate, expiryDate);
        return period.getYears() * 12 + period.getMonths();
    }

    // 카드 활용도를 계산함
    public int calculatePerformanceRate(long performanceCondition, double monthlyUsage) {
        double performanceRate = monthlyUsage / performanceCondition * 100;
        if (performanceRate > 100) {
            performanceRate = 100;
        }
        return (int) performanceRate;
    }


    @Override
    public UserCardDetailDTO findUserCardDetail(long productId, String userId) {
        // 사용자 정보를 가져옴
        UserCardDTO userCardDTO = cardMapper.findUserCardDetailById(productId, userId);
        // 카드 계약 기간 계산
        int term = calculateTerm(userCardDTO.getIssueDate(), userCardDTO.getExpiryDate());
        // 카드 실적율 계산
        int performanceRate = calculatePerformanceRate(userCardDTO.getPerformanceCondition(), userCardDTO.getMonthlyUsage());
        // 반환할 객체 생성
        UserCardDetailDTO userCardDetailDTO = UserCardDetailDTO.custom(userCardDTO, term, performanceRate);
        // null 일 경우 예외 아니면 해당 객체 반환
        return Optional.ofNullable(userCardDetailDTO)
                .orElseThrow(NoSuchElementException::new);
    }
}
