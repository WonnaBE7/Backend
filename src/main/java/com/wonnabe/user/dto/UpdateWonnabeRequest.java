package com.wonnabe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWonnabeRequest {
    private List<Integer> selectedWonnabeIds;
}