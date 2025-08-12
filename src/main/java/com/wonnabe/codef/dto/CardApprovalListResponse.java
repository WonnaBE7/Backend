package com.wonnabe.codef.dto;

import com.wonnabe.codef.domain.CardTransactions;
import com.wonnabe.codef.mapper.AssetCardMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
public class CardApprovalListResponse {
    private List<CardTransactionData> data;

    public List<CardTransactions> toCardTransactions(String userId,
                                                     String institutionCode,
                                                     AssetCardMapper assetCardMapper) {
        List<CardTransactions> list = new ArrayList<>();

        for (CardTransactionData tx : data) {
            CardTransactions entity = new CardTransactions();
            entity.setUserId(userId);

            // 1) userCardId 결정 (기존 로직 유지, 변수명만 명확화)
            String normalized = normalizeCardName(tx.getResCardName());
            Long userCardId = assetCardMapper.findUserCardIdByKeyword(userId, normalized, institutionCode);

            if (userCardId == null) {
                userCardId = assetCardMapper.findUserCardIdByTwoCardNumbers(userId, tx.getResCardNo(), tx.getResCardNo1());
                if (userCardId == null) {
                    userCardId = 9999L; // 미매핑 표식
                }
            }
            entity.setCardId(userCardId);

            // 거래일/시간 변환
            if (tx.getResUsedDate() != null && !tx.getResUsedDate().isBlank()) {
                entity.setTransactionDate(LocalDate.parse(tx.getResUsedDate(), DateTimeFormatter.ofPattern("yyyyMMdd")));
            }
            if (tx.getResUsedTime() != null && !tx.getResUsedTime().isBlank()) {
                entity.setTransactionTime(LocalTime.parse(tx.getResUsedTime(), DateTimeFormatter.ofPattern("HHmmss")));
            }

            if (tx.getResCardNo1() != null && !tx.getResCardNo1().isBlank()) {
                entity.setCardNumber(tx.getResCardNo1());
            } else {
                entity.setCardNumber(tx.getResCardNo());
            }

            entity.setCardName(tx.getResCardName());
            entity.setMerchantName(tx.getResMemberStoreName());
            entity.setMerchantStoreType(tx.getResMemberStoreType());
            entity.setMerchantCategory(mapMerchantCategory(tx.getResMemberStoreType()));
            entity.setMerchantCorpNo(tx.getResMemberStoreCorpNo());
            entity.setMerchantStoreNo(tx.getResMemberStoreNo());
            entity.setAmount(new BigDecimal(tx.getResUsedAmount()).negate());

            // 5) card_name 보강 로직
            if (entity.getCardName() == null || entity.getCardName().isBlank()) {
                String resolvedName = null;

                // 5-1. userCardId로 조회 (정확도 가장 높음)
                if (userCardId != null && !userCardId.equals(9999L)) {
                    resolvedName = assetCardMapper.findCardNameByUserCardId(userCardId);
                }

                // 5-2. 카드번호 뒤4자리로 조회 (fallback)
                if ((resolvedName == null || resolvedName.isBlank()) && entity.getCardNumber() != null) {
                    String last4 = extractLast4Digits(entity.getCardNumber());
                    if (last4 != null) {
                        resolvedName = assetCardMapper.findCardNameByLast4(userId, last4);
                    }
                }

                if (resolvedName != null && !resolvedName.isBlank()) {
                    entity.setCardName(resolvedName);
                }
            }

            list.add(entity);
        }

        return list;
    }

    private static String extractLast4Digits(String raw) {
        if (raw == null || raw.isBlank()) return null;
        // 숫자만 남기고 뒤 4자리 추출 (마스킹/띄어쓰기/하이픈 대응)
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return null;
        return digits.substring(digits.length() - 4);
    }

    private String normalizeCardName(String rawCardName) {
        if (rawCardName == null) return "";

        return rawCardName
                .replace("KB국민", "")                   // 브랜드명 제거
                .replace("RF", "")                   // 브랜드명 제거
                .replaceAll("\\s+", "")                  // 공백 제거
                .replaceAll("[()\\[\\]{}]", "")          // 괄호류 제거
                .replace("nori", "노리");                // 수동 매핑 예시
    }

    private static final Set<String> FOOD_TYPES = new HashSet<>(Arrays.asList(
            "일반음식점 기타","일반음식점","한식","중식전문점","서양식전문점",
            "휴게음식점","커피/음료전문점","커피전문점","제과.제빵",
            "유흥주점","일반주점", "농.수.축산물점","정육점","식품류 제조/도매업","기타 식품"
    ));

    private static final Set<String> TRANSPORT_TYPES = new HashSet<>(Arrays.asList(
            "택시","지하철","시내버스","항공사","렌트카","주유소"
    ));

    private static final Set<String> SHOPPING_TYPES = new HashSet<>(Arrays.asList(
            "편의점","전자상거래PG","전자상거래PG상품권","전자상거래오픈마켓",
            "PG일반(인증)","PG일반(비인증)","온라인상품권",
            "안경.광학제품","기타잡화"
    ));

    // 새로 추가된 문화 카테고리(정확 일치 후보)
    private static final Set<String> CULTURE_TYPES = new HashSet<>(Arrays.asList(
            "영화관","공연/전시","문화센터","박물관","미술관","서점","도서","음반/비디오","티켓/예매"
    ));

    private CardTransactions.MerchantCategory mapMerchantCategory(String storeType) {
        if (storeType == null) return CardTransactions.MerchantCategory.other;

        String s = storeType.trim();
        if (s.isEmpty()) return CardTransactions.MerchantCategory.other;

        // 1) 정확 일치 우선
        if (FOOD_TYPES.contains(s)) return CardTransactions.MerchantCategory.food;
        if (TRANSPORT_TYPES.contains(s)) return CardTransactions.MerchantCategory.transport;
        if (SHOPPING_TYPES.contains(s)) return CardTransactions.MerchantCategory.shopping;
        if (CULTURE_TYPES.contains(s)) return CardTransactions.MerchantCategory.culture;

        // 2) 휴리스틱(부분문자열)
        // 음식점/전문점(안경 전문점은 쇼핑으로 분류)
        if (s.contains("음식점") || (s.endsWith("전문점") && !s.contains("안경"))) {
            return CardTransactions.MerchantCategory.food;
        }
        // 교통
        if (s.contains("택시") || s.contains("버스") || s.contains("지하철")
                || s.contains("항공") || s.contains("렌트") || s.contains("주유")) {
            return CardTransactions.MerchantCategory.transport;
        }
        // 쇼핑/전자결제/상품권/오픈마켓/편의점/소매
        if (s.contains("PG") || s.contains("상품권") || s.contains("오픈마켓")
                || s.contains("편의점") || s.contains("안경") || s.contains("잡화")
                || s.contains("농") || s.contains("축산") || s.contains("정육")
                || s.contains("제과") || s.contains("제빵")) {
            return CardTransactions.MerchantCategory.shopping;
        }
        // 문화(도서/공연/전시/영화/티켓 등)
        if (s.contains("영화") || s.contains("극장") || s.contains("공연") || s.contains("전시")
                || s.contains("미술관") || s.contains("박물관") || s.contains("콘서트")
                || s.contains("연극") || s.contains("도서") || s.contains("서점")
                || s.contains("음반") || s.contains("비디오") || s.contains("예매")
                || s.contains("티켓") || s.contains("문화")) {
            return CardTransactions.MerchantCategory.culture;
        }

        return CardTransactions.MerchantCategory.other;
    }
}
