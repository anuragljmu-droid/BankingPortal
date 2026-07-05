package com.banking_portal.account_management_api.exception;

public class ExternalLoggingException extends RuntimeException {

    public ExternalLoggingException(String message, Throwable cause) {
        super(message, cause);
    }
}
