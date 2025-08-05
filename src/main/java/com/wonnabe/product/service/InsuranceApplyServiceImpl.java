package com.wonnabe.product.service;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.dto.InsuranceApplyRequestDTO;
import com.wonnabe.product.mapper.InsuranceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

@Log4j2
@Service
@RequiredArgsConstructor
public class InsuranceApplyServiceImpl implements InsuranceApplyService {

    private final InsuranceMapper insuranceMapper;

    @Override
    @Transactional
    public void applyUserInsurance(InsuranceApplyRequestDTO insuranceApplyRequestDTO, String userId) {
        // 1. 보험 상품 정보 조회
        InsuranceProductVO product = insuranceMapper.findById(Long.parseLong(insuranceApplyRequestDTO.getInsuranceId()));
        if (product == null) {
            throw new NoSuchElementException("해당 보험 상품을 찾을 수 없습니다.");
        }

        // 2. 사용자 성별 정보 조회
        String gender = insuranceMapper.findGenderByUserId(userId);
        if (gender == null || (!gender.equals("F") && !gender.equals("M"))) {
            throw new IllegalStateException("사용자 성별 정보가 유효하지 않습니다.");
        }

        // 3. 성별에 따른 월 보험료 선택
        BigDecimal selectedPremium = "F".equals(gender) ? product.getFemalePremium() : product.getMalePremium();

        // 4. 만료일 설정 (예: 10년 뒤)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 10);
        Date expiryDate = calendar.getTime();

        // 5. 사용자 보험 가입 정보 생성 (Builder 사용)
        UserInsuranceVO insurance = UserInsuranceVO.builder()
                .userId(userId)
                .productId(product.getProductId())
                .startDate(new java.sql.Date(new Date().getTime())) // java.sql.Date로 변환
                .endDate(new java.sql.Date(expiryDate.getTime()))   // java.sql.Date로 변환
                .monthlyPremium(selectedPremium) // 선택된 보험료 설정
                .build();

        // 6. 사용자 보험 가입
        insuranceMapper.insertUserInsurance(insurance);
        long newInsuranceId = insurance.getId();
        if (newInsuranceId == 0) {
             throw new IllegalStateException("보험 가입에 실패했습니다. (ID 생성 오류)");
        }

        // 7. 사용자 정보에 보험 ID 추가
        insuranceMapper.updateUserInsuranceInfo(newInsuranceId, userId);

        // 8. 가입 확인 (선택적)
        UserInsuranceVO insuranceCheck = insuranceMapper.findUserInsuranceByProductId(product.getProductId(), userId);
        if (insuranceCheck == null) {
            throw new IllegalStateException("보험 가입 후 사용자 정보 업데이트에 실패했습니다.");
        }
        log.info("보험 가입 완료: userId={}, insuranceId={}", userId, newInsuranceId);
    }
}
