package com.wonnabe.product.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * User_Savings 테이블과 매핑되는 VO 클래스
 * 사용자의 예적금 가입 정보를 담는 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavingsVO {

    private Long id;                // 고유 ID (PK)
    private String userId;            // 사용자 ID (FK)
    private Long productId;         // 상품 ID (FK)

    private Long principalAmount;   // 원금
    private Long currentBalance;    // 현재 잔액 !!! 추가된 것(컬럼에없음)
    private Date startDate;         // 가입일
    private Date maturityDate;      // 만기일

    private Long monthlyPayment;    // 월납입액

    // MyBatis resultMap의 <association>을 통해 채워질 연관 객체
    private SavingsProductVO savingsProduct;

}