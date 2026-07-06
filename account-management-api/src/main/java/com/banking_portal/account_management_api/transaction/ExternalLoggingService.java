package com.banking_portal.account_management_api.transaction;

import com.banking_portal.account_management_api.exception.ExternalLoggingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExternalLoggingService {

    private final RestClient restClient;
    private final String externalLoggingUrl;

    public ExternalLoggingService(
            RestClient restClient,
            @Value("${banking.external-logging.url}") String externalLoggingUrl
    ) {
        this.restClient = restClient;
        this.externalLoggingUrl = externalLoggingUrl;
    }

    public void logDebitAttempt() {
        try {
            restClient.get()
                    .uri(externalLoggingUrl)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new ExternalLoggingException("External debit logging failed", exception);
        }
    }
}
