package com.wonnabe.product.service;

import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.domain.UserSavingsVO;
import com.wonnabe.product.dto.SavingsApplyRequestDTO;
import com.wonnabe.product.mapper.SavingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

@Log4j2
@Service
@RequiredArgsConstructor
public class SavingsApplyServiceImpl implements SavingsApplyService {

    private final SavingsMapper savingsMapper;

    @Override
    @Transactional
    public void applyUserSavings(SavingsApplyRequestDTO requestDTO, String userId) {
        // 1. 예적금 상품 정보 조회
        SavingsProductVO product = savingsMapper.findById(requestDTO.getProductId());
        if (product == null) {
            throw new NoSuchElementException("해당 예적금 상품을 찾을 수 없습니다.");
        }

        // 2. 만기일 계산
        Date startDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, requestDTO.getJoinPeriod());
        Date maturityDate = calendar.getTime();

        // 3. 사용자 예적금 가입 정보 생성
        UserSavingsVO savings = UserSavingsVO.builder()
                .userId(userId)
                .productId(product.getProductId())
                .principalAmount(requestDTO.getPrincipalAmount())
                .startDate(startDate)
                .maturityDate(maturityDate)
                .monthlyPayment(requestDTO.getMonthlyPayment())
                .build();

        // 4. 사용자 예적금 가입
        savingsMapper.insertUserSavings(savings);
        long newSavingsId = savings.getId();
        if (newSavingsId == 0) {
            throw new IllegalStateException("예적금 가입에 실패했습니다. (ID 생성 오류)");
        }

        // 5. 사용자 정보에 예적금 ID 추가
        savingsMapper.updateUserSavingsInfo(product.getProductId(), userId);

        // 6. 가입 확인 (선택적)
        UserSavingsVO savingsCheck = savingsMapper.findUserSavingsByProductId(product.getProductId(), userId);
        if (savingsCheck == null) {
            throw new IllegalStateException("예적금 가입 후 사용자 정보 업데이트에 실패했습니다.");
        }
        log.info("예적금 가입 완료: userId={}, savingsId={}", userId, newSavingsId);
    }
}
