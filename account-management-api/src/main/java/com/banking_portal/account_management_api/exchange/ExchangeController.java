package com.banking_portal.account_management_api.exchange;

import com.banking_portal.account_management_api.dto.ExchangeRequest;
import com.banking_portal.account_management_api.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Operation(summary = "Exchange money between two current-user accounts")
    @PostMapping
    public List<TransactionResponse> performExchange(@Valid @RequestBody ExchangeRequest request) {
        return exchangeService.performExchange(request);
    }
}
