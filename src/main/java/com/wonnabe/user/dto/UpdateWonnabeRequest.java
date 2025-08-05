package com.wonnabe.user.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateWonnabeRequest {
    private List<Integer> selected_wonnabe_ids;
}