package com.banking_portal.account_management_api.exchange;

import com.banking_portal.account_management_api.common.CurrencyCode;
import com.banking_portal.account_management_api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        if (sourceCurrency == targetCurrency) {
            return BigDecimal.ONE;
        }

        return exchangeRateRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange rate not found"));
    }
}
