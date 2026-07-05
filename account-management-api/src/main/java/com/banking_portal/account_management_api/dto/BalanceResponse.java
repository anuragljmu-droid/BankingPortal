package com.banking_portal.account_management_api.dto;

import com.banking_portal.account_management_api.common.CurrencyCode;

import java.math.BigDecimal;

public record BalanceResponse(
        Long accountId,
        BigDecimal balance,
        CurrencyCode currency
) {
}
