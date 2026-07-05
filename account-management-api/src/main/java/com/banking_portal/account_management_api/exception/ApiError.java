package com.banking_portal.account_management_api.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        String message,
        int status,
        Instant timestamp,
        Map<String, String> validationErrors
) {

    public static ApiError of(String message, int status) {
        return new ApiError(message, status, Instant.now(), Map.of());
    }

    public static ApiError of(String message, int status, Map<String, String> validationErrors) {
        return new ApiError(message, status, Instant.now(), validationErrors);
    }
}
