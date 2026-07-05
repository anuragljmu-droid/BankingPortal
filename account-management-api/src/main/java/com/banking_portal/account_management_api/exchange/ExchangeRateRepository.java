package com.banking_portal.account_management_api.exchange;

import com.banking_portal.account_management_api.common.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findBySourceCurrencyAndTargetCurrency(
            CurrencyCode sourceCurrency,
            CurrencyCode targetCurrency
    );
}
