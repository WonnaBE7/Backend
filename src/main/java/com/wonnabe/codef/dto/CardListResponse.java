package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CardListResponse {

    @JsonProperty("resCardName")
    private String resCardName;

    @JsonProperty("resCardNo")
    private String resCardNo;

    @JsonProperty("resCardType")
    private String resCardType;

    @JsonProperty("resUserNm")
    private String resUserNm;

    @JsonProperty("resSleepYN")
    private String resSleepYN;

    @JsonProperty("resTrafficYN")
    private String resTrafficYN;

    @JsonProperty("resValidPeriod")
    private String resValidPeriod;

    @JsonProperty("resIssueDate")
    private String resIssueDate;

    @JsonProperty("resImageLink")
    private String resImageLink;

    @JsonProperty("resState")
    private String resState;
}
