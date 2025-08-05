package com.wonnabe.goal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wonnabe.goal.domain.RecommendedProductVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProductDTO {
    private Long id;
    private String name;
    private String bank;
    private String category; // 예금, 적금
    private BigDecimal interestRate;
    private Integer achievementRate;
    private BigDecimal saveAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate expectedAchievementDate;
    private BigDecimal expectedTotalAmount;

    public static RecommendedProductDTO of(RecommendedProductVO vo) {
        return vo == null ? null : RecommendedProductDTO.builder()
                .id(vo.getId())
                .name(vo.getProductName())
                .bank(vo.getBankName())
                .category(determineCategory(vo.getProductId()))
                .interestRate(vo.getInterestRate())
                .achievementRate(
                        vo.getAchievementRate() != null
                                ? vo.getAchievementRate().intValue()
                                : null
                )
                .saveAmount(vo.getSaveAmount())
                .expectedAchievementDate(vo.getExpectedAchievementDate())
                .expectedTotalAmount(vo.getExpectedTotalAmount())
                .build();
    }

    public static List<RecommendedProductDTO> ofList(List<RecommendedProductVO> voList) {
        return voList == null ? null : voList.stream()
                .map(RecommendedProductDTO::of)
                .toList();
    }

    private static String determineCategory(Long productId) {
        if (productId == null) {
            return null;
        }

        if (productId >= 1000L && productId < 1500L) {
            return "적금";
        } else if (productId >= 1500L && productId < 2000L) {
            return "예금";
        }

        return null;
    }
}
