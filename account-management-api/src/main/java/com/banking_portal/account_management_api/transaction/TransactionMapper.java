package com.banking_portal.account_management_api.transaction;

import com.banking_portal.account_management_api.dto.BalanceHistoryResponse;
import com.banking_portal.account_management_api.dto.TransactionResponse;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getBalanceAfter(),
                transaction.getAccountVersionAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getReference(),
                transaction.getRelatedTransactionId()
        );
    }

    public static BalanceHistoryResponse toBalanceHistoryResponse(Transaction transaction) {
        return new BalanceHistoryResponse(
                transaction.getCreatedAt(),
                transaction.getBalanceAfter(),
                transaction.getStatus()
        );
    }
}
