package com.wonnabe.product.service;

import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.CardApplyRequestDTO;
import com.wonnabe.product.dto.UserCardDTO;
import com.wonnabe.product.dto.UserCardDetailDTO;
import com.wonnabe.product.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

@Log4j2
@Service("cardServiceImpl")
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

    // 카드 번호 생성 함수
    private String getNextCardNumber() {
        String lastCardNumber = cardMapper.findLastCardNumber();
        long nextNum = 1L;

        if (lastCardNumber != null) {
            String digits = lastCardNumber.replaceAll("-", "");
            nextNum = Long.parseLong(digits) + 1;
        }

        String nextDigits = String.format("%016d", nextNum);  // 항상 16자리 유지
        return nextDigits.replaceAll("(.{4})(?=.)", "$1-");
    }

    @Override
    @Transactional
    public void applyUserCard(CardApplyRequestDTO cardApplyRequestDTO, String userId){
        Calendar calendar = Calendar.getInstance(); // 현재 날짜
        calendar.add(Calendar.YEAR, 5); // 5년 뒤 카드 만료
        // 계좌 Id 조회
        Long accountId = cardMapper.getAccountId(cardApplyRequestDTO.getLinkedAccount(), userId);
        if (accountId == null) {
            throw new NoSuchElementException("해당 계좌를 보유하고 있지 않습니다.");
        }

        // 마지막으로 입력된 카드 번호 조회
        String lastCardNumber = getNextCardNumber();

        // 카드 상품 조회
        CardProductVO product = cardMapper.findById(Long.parseLong(cardApplyRequestDTO.getCardId()));

        // 사용자에 맞는 형식으로 변환
        UserCardVO card = UserCardVO.builder()
                .userId(userId)
                .productId(product.getProductId())
                .monthlyUsage(0)
                .issueDate(new Date())
                .expiryDate(calendar.getTime())
                .performanceCondition(product.getPerformanceCondition())
                .cardNumber(lastCardNumber)
                .accountId(accountId)
                .build();

        // 사용자 카드 신청
        cardMapper.insertUserCard(card);
        // 신청 후 발급 된 아이디 확인
        long id = card.getId();
        // 사용자 정보에 카드 추가
        cardMapper.updateUserCardInfo(id, card.getUserId());

        // 사용자 카드 목록 확인
        String myCardIds = cardMapper.getMyCardIdsJson(card.getUserId());
        // 사용자가 등록한 카드 확인
        UserCardVO cardCheck = cardMapper.findUserCardByproductId(card.getProductId(), card.getUserId());
        // 비워 있을 시 등록 실패 반환
        if (cardCheck == null && myCardIds.isEmpty()) {
            throw new IllegalStateException("카드 등록에 실패했습니다.");
        }
    }
}
