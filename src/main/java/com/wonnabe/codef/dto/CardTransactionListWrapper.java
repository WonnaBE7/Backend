package com.wonnabe.codef.dto;

import lombok.Data;
import java.util.List;

@Data
public class CardTransactionListWrapper {
    private CardTransactionResult result;
    private String endpoint;
    private List<CardTransactionData> data;
    private String userId;
    private String connectedId;
}
