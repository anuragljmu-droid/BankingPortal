package com.banking_portal.account_management_api.dto;

import com.banking_portal.account_management_api.common.CurrencyCode;
import com.banking_portal.account_management_api.transaction.TransactionStatus;
import com.banking_portal.account_management_api.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long accountId,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        CurrencyCode currency,
        BigDecimal balanceAfter,
        Long accountVersionAfter,
        String description,
        Instant createdAt,
        String reference,
        Long relatedTransactionId
) {
}
