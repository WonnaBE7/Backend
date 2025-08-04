package com.wonnabe.product.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * 사용자가 가입한 보험 상품의 정보를 담는 VO(Value Object) 클래스.
 * user_insurance 테이블의 데이터를 나타내며,
 * 보험 상품의 상세 정보를 담고 있는 {@link InsuranceProductVO}를 포함
 */
@Data
public class UserInsuranceVO {

    private Long id;
    private String userId;
    private Long productId;
    private BigDecimal monthlyPremium;
    private Date startDate;
    private Date endDate;
    private BigDecimal totalPaid;
    private java.time.LocalDateTime createdAt;

    /**
     * 이 가입 정보에 해당하는 보험 상품의 상세 정보.
     * MyBatis의 resultMap을 통해 조인된 결과가 매핑.
     */
    private InsuranceProductVO product;
}

