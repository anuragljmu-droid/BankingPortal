package com.banking_portal.account_management_api.transaction;

import com.banking_portal.account_management_api.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Get one transaction")
    @GetMapping("/{transactionId}")
    public TransactionResponse getTransaction(@PathVariable Long transactionId) {
        return transactionService.getTransaction(transactionId);
    }

    @Operation(summary = "Get structured transaction summary for PDF export")
    @GetMapping("/{transactionId}/summary")
    public TransactionResponse getTransactionSummary(@PathVariable Long transactionId) {
        return transactionService.getTransactionSummary(transactionId);
    }
}
