package com.wonnabe.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalStatusUpdateRequestDTO {
    private Long selectedProductId; // Optional

    @NotBlank(message = "status는 필수입니다")
    private String status;

    public void validate() {
        if ("PUBLISHED".equals(status) && selectedProductId == null) {
            throw new IllegalArgumentException("PUBLISHED 상태로 변경할 때는 selectedProductId가 필수입니다");
        }

        if ("ACHIEVED".equals(status) && selectedProductId != null) {
            throw new IllegalArgumentException("ACHIEVED 상태로 변경할 때는 selectedProductId를 포함할 수 없습니다");
        }
    }
}
