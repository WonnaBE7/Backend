package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingListResponse {

    @JsonProperty("resAccount")
    private String resAccount;

    @JsonProperty("resTrHistoryList")
    private List<Map<String, Object>> resTrHistoryList;
}
