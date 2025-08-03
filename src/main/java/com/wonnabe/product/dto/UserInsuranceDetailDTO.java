package com.wonnabe.product.dto;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.UserInsuranceVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * 보유 보험 상품 상세 조회 API의 응답을 위한 DTO(Data Transfer Object) 클래스.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInsuranceDetailDTO {

    // InsuranceProductVO 에서 가져오는 정보
    private String providerName;
    private String productName;
    private String coverageType;
    private String coverageDesc;
    private String coverageLimit;

    // UserInsuranceVO 에서 가져오는 정보
    private Long productId;
    private BigDecimal monthlyPremium;
    private Date startDate;
    private Date endDate;
    private BigDecimal totalPaid;

    /**
     * UserInsuranceVO 객체를 UserInsuranceDetailDTO로 변환하는 정적 메소드.
     * @param vo 변환할 VO 객체
     * @return 변환된 DTO 객체
     */
    public static UserInsuranceDetailDTO from(UserInsuranceVO vo) {
        // product가 null일 경우를 대비한 방어 코드
        InsuranceProductVO product = vo.getProduct() != null ? vo.getProduct() : new InsuranceProductVO();
        
        return UserInsuranceDetailDTO.builder()
                .providerName(product.getProviderName())
                .productName(product.getProductName())
                .coverageType(product.getCoverageType())
                .coverageDesc(product.getCoverageDesc())
                .coverageLimit(product.getCoverageLimit())
                .productId(vo.getId())
                .monthlyPremium(vo.getMonthlyPremium())
                .startDate(vo.getStartDate())
                .endDate(vo.getEndDate())
                .totalPaid(vo.getTotalPaid())
                .build();
    }
}
