package com.wonnabe.nowme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NowMeRequestDTO {

    private List<Integer> answers;  // 1~5점 척도, 총 12문항
}