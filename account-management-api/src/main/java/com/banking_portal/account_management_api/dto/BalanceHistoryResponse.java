package com.banking_portal.account_management_api.dto;

import com.banking_portal.account_management_api.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceHistoryResponse(
        Instant timestamp,
        BigDecimal balance,
        TransactionStatus status
) {
}
