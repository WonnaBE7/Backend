package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserTransaction;

import java.util.List;

public interface UserTransactionsMapper {
    void upsertBatch(List<UserTransaction> transactions);
}

