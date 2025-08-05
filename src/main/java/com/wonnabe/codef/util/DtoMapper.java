//package com.wonnabe.asset.util;
//
//import com.wonnabe.asset.domain.UserAccount;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class DtoMapper {
//
//    public List<UserAccount> toUserAccounts(List<CodefAccountDto> dtos, String userId, String institutionCode) {
//        return dtos.stream().map(dto -> {
//            UserAccount entity = new UserAccount();
//            entity.setUserId(userId);
//            entity.setAccountNumber(dto.getResAccount());
//            entity.setAccountName(dto.getResAccountName());
//            entity.setCategory(mapDepositCodeToCategory(dto.getResAccountDeposit()));
//            entity.setBalance(new BigDecimal(dto.getResAccountBalance()));
//            entity.setInstitutionCode(institutionCode);
//            entity.setLastUpdated(LocalDateTime.now());
//            return entity;
//        }).collect(Collectors.toList());
//    }
//
//    private String mapDepositCodeToCategory(String code) {
//        return switch (code) {
//            case "11" -> "입출금";
//            case "12", "13" -> "기타";     // 적금, 신탁
//            case "30" -> "투자";           // 펀드
//            case "40" -> "연금";           // 대출
//            case "50" -> "연금";           // 보험
//            default -> "기타";
//        };
//    }
//}
