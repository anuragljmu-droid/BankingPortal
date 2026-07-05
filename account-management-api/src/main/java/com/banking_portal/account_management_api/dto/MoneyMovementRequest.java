package com.banking_portal.account_management_api.dto;

import com.banking_portal.account_management_api.common.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MoneyMovementRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String description,
        CurrencyCode currency
) {
}
