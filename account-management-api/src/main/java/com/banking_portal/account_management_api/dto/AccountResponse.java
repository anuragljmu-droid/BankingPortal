package com.banking_portal.account_management_api.dto;

import com.banking_portal.account_management_api.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String accountNumber,
        CurrencyCode currency,
        BigDecimal balance,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
