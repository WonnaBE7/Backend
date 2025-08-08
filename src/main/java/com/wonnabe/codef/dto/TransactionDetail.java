package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDetail {

    @JsonProperty("resAccountTrDate")
    private String transactionDate;

    @JsonProperty("resAccountTrTime")
    private String transactionTime;

    @JsonProperty("resAccountOut")
    private String withdrawalAmount;

    @JsonProperty("resAccountIn")
    private String depositAmount;

    @JsonProperty("resAccountDesc1")
    private String description1;

    @JsonProperty("resAccountDesc2")
    private String description2;

    @JsonProperty("resAccountDesc3")
    private String description3;

    @JsonProperty("resAccountDesc4")
    private String description4;

//    @JsonProperty("resAfterTranBalance")
//    private String balanceAfter;
//
//    @JsonProperty("tranDesc")
//    private String transactionDescription;

}
