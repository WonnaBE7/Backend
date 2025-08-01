package com.wonnabe.nowme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NowMeRequestDTO {

//    @NotNull(message = "설문 답변은 필수입니다.")
//    @Size(min = 12, max = 12, message = "정확히 12개 문항에 답변해주세요.")
    private List<Integer> answers;  // 1~5점 척도, 총 12문항
}