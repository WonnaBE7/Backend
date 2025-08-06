package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserTransactions;

import java.util.List;

public interface UserTransactionsMapper {
    void upsertBatch(List<UserTransactions> transactions);
}

