package com.banking_portal.account_management_api.transaction;

import com.banking_portal.account_management_api.exception.ExternalLoggingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExternalLoggingService {

    private static final String EXTERNAL_LOGGING_URL = "https://httpstat.us/200";

    private final RestClient restClient;

    public ExternalLoggingService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void logDebitAttempt() {
        try {
            restClient.get()
                    .uri(EXTERNAL_LOGGING_URL)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new ExternalLoggingException("External debit logging failed", exception);
        }
    }
}
