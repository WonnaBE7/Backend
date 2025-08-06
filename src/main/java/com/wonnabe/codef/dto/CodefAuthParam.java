package com.wonnabe.codef.dto;

import lombok.Data;

@Data
public class CodefAuthParam {
    private Long id;                             // 식별자
    private String userId;                       // 유저 UUID
    private String institutionCode;              // 기관 코드
    private String birthDate;                    // 생년월일 (YYYYMMDD)
    private String account;                      // 계좌번호
    private String accountPassword;              // 계좌 비밀번호 (RSA 암호화)
    private String cardNo;                       // 카드번호
    private String cardPassword;                 // 카드 비밀번호 앞 2자리
    private String cardName;                     // 카드명
    private String duplicateCardIdx;             // 중복 카드 일련번호
    private String startDate;                    // 시작일자 (YYYYMMDD or YYYYMM)
    private String endDate;                      // 종료일자 (YYYYMMDD or YYYYMM)
    private String orderBy;                      // 일자 정렬순서 ("0": 최신, "1": 과거)
    private String inquiryType;                  // 조회 구분 (0,1)
    private String memberStoreInfoType;          // 가맹점 정보 포함 여부 (0~3)
    private String withdrawAccountNo;            // 출금 계좌 번호
    private String withdrawAccountPassword;      // 출금 계좌 비밀번호 (RSA)
    private String subAccountId;                 // 복수 계정 ID
    private String subAccountPassword;           // 복수 계정 패스워드
    private String inquiryPurpose;               // 파라미터 용도 (API 설명)
    private String endpoint;                     // CODEF API 호출 주소

    // 아래 두 필드는 DB에는 없고 Java 내부 로직에서만 사용됨
    private String accessToken;                  // CODEF access token (인증 토큰)
    private String connectedId;                  // CODEF connectedId
}
