package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectedIdResponseDto {

    @JsonProperty("result")
    private Result result;

    @JsonProperty("data")
    private DataSection data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String code;
        private String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataSection {
        @JsonProperty("connectedId")
        private String connectedId;
    }
}

