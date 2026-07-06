package com.banking_portal.account_management_api.account;

import com.banking_portal.account_management_api.common.TransactionType;
import com.banking_portal.account_management_api.dto.AccountResponse;
import com.banking_portal.account_management_api.dto.BalanceHistoryResponse;
import com.banking_portal.account_management_api.dto.BalanceResponse;
import com.banking_portal.account_management_api.dto.MoneyMovementRequest;
import com.banking_portal.account_management_api.dto.TransactionResponse;
import com.banking_portal.account_management_api.exception.BadRequestException;
import com.banking_portal.account_management_api.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Operation(summary = "Get all accounts for the current demo user")
    @GetMapping
    public List<AccountResponse> getAccounts() {
        return accountService.getCurrentUserAccounts();
    }

    @Operation(summary = "Get one account")
    @GetMapping("/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId) {
        return accountService.getAccount(accountId);
    }

    @Operation(summary = "Get account balance")
    @GetMapping("/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable Long accountId) {
        return accountService.getBalance(accountId);
    }

    @Operation(summary = "Deposit to or debit from an account")
    @PostMapping("/{accountId}")
    public TransactionResponse moveMoney(
            @PathVariable Long accountId,
            @RequestParam TransactionType action,
            @Valid @RequestBody MoneyMovementRequest request
    ) {
        return switch (action) {
            case TransactionType.DEPOSIT-> transactionService.deposit(accountId, request);
            case TransactionType.DEBIT -> transactionService.debit(accountId, request);
        };
    }

    @Operation(summary = "Get paginated transaction history for an account")
    @GetMapping("/{accountId}/transactions")
    public Page<TransactionResponse> getTransactions(@PathVariable Long accountId, Pageable pageable) {
        return transactionService.getTransactions(accountId, pageable);
    }

    @Operation(summary = "Get account balance history")
    @GetMapping("/{accountId}/balance-history")
    public List<BalanceHistoryResponse> getBalanceHistory(@PathVariable Long accountId) {
        return transactionService.getBalanceHistory(accountId);
    }
}
