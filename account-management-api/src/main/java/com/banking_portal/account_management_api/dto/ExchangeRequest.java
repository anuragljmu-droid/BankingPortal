package com.banking_portal.account_management_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExchangeRequest(
        @NotNull Long fromAccountId,
        @NotNull Long toAccountId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String description
) {
}
