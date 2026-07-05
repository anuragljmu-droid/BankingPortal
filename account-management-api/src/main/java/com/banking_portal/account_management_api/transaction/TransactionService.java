package com.banking_portal.account_management_api.transaction;

import com.banking_portal.account_management_api.account.Account;
import com.banking_portal.account_management_api.account.AccountRepository;
import com.banking_portal.account_management_api.account.AccountService;
import com.banking_portal.account_management_api.common.CurrencyCode;
import com.banking_portal.account_management_api.dto.BalanceHistoryResponse;
import com.banking_portal.account_management_api.dto.MoneyMovementRequest;
import com.banking_portal.account_management_api.dto.TransactionResponse;
import com.banking_portal.account_management_api.exception.BadRequestException;
import com.banking_portal.account_management_api.exception.ExternalLoggingException;
import com.banking_portal.account_management_api.exception.InsufficientFundsException;
import com.banking_portal.account_management_api.exception.ResourceNotFoundException;
import com.banking_portal.account_management_api.exchange.ExchangeRateService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Supplier;

@Service
public class TransactionService {

    private static final int MAX_BALANCE_UPDATE_ATTEMPTS = 3;

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;
    private final ExternalLoggingService externalLoggingService;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            AccountService accountService,
            ExchangeRateService exchangeRateService,
            ExternalLoggingService externalLoggingService,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.exchangeRateService = exchangeRateService;
        this.externalLoggingService = externalLoggingService;
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse deposit(Long accountId, MoneyMovementRequest request) {
        return retryOnOptimisticLock(() -> depositOnce(accountId, request));
    }

    public TransactionResponse debit(Long accountId, MoneyMovementRequest request) {
        return retryOnOptimisticLock(() -> debitOnce(accountId, request));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long accountId, Pageable pageable) {
        accountService.getCurrentUserAccount(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TransactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long transactionId) {
        return TransactionMapper.toResponse(getCurrentUserTransaction(transactionId));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionSummary(Long transactionId) {
        return getTransaction(transactionId);
    }

    @Transactional(readOnly = true)
    public List<BalanceHistoryResponse> getBalanceHistory(Long accountId) {
        accountService.getCurrentUserAccount(accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtAsc(accountId)
                .stream()
                .map(TransactionMapper::toBalanceHistoryResponse)
                .toList();
    }

    @Transactional(noRollbackFor = {
            BadRequestException.class,
            ExternalLoggingException.class,
            InsufficientFundsException.class
    })
    protected TransactionResponse depositOnce(Long accountId, MoneyMovementRequest request) {
        Account account = accountService.getCurrentUserAccount(accountId);
        CurrencyCode requestCurrency = request.currency() == null ? account.getCurrency() : request.currency();
        BigDecimal accountCurrencyAmount = convertAmount(request.amount(), requestCurrency, account.getCurrency());
        Transaction transaction = createInProgressTransaction(
                account,
                TransactionType.DEPOSIT,
                accountCurrencyAmount,
                account.getCurrency(),
                request.description()
        );

        account.setBalance(account.getBalance().add(accountCurrencyAmount));
        Account savedAccount = accountRepository.saveAndFlush(account);
        markSuccess(transaction, savedAccount);
        return TransactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional(noRollbackFor = {
            BadRequestException.class,
            ExternalLoggingException.class,
            InsufficientFundsException.class
    })
    protected TransactionResponse debitOnce(Long accountId, MoneyMovementRequest request) {
        Account account = accountService.getCurrentUserAccount(accountId);
        if (request.currency() != null && request.currency() != account.getCurrency()) {
            throw new BadRequestException("Debit currency must match account currency");
        }

        Transaction transaction = createInProgressTransaction(
                account,
                TransactionType.DEBIT,
                request.amount(),
                account.getCurrency(),
                request.description()
        );

        try {
            externalLoggingService.logDebitAttempt();
            if (account.getBalance().compareTo(request.amount()) < 0) {
                markFailed(transaction, account, TransactionStatus.FAILED_INSUFFICIENT_BALANCE);
                throw new InsufficientFundsException("Insufficient funds");
            }

            account.setBalance(account.getBalance().subtract(request.amount()));
            Account savedAccount = accountRepository.saveAndFlush(account);
            markSuccess(transaction, savedAccount);
            return TransactionMapper.toResponse(transactionRepository.save(transaction));
        } catch (ExternalLoggingException exception) {
            markFailed(transaction, account, TransactionStatus.FAILED);
            throw exception;
        }
    }

    private Transaction createInProgressTransaction(
            Account account,
            TransactionType type,
            BigDecimal amount,
            CurrencyCode currency,
            String description
    ) {
        Transaction transaction = new Transaction(
                account,
                type,
                TransactionStatus.IN_PROGRESS,
                amount,
                currency,
                account.getBalance(),
                description
        );
        transaction.setAccountVersionAfter(account.getVersion());
        return transactionRepository.saveAndFlush(transaction);
    }

    private Transaction getCurrentUserTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!transaction.getAccount().getUser().getId().equals(AccountService.CURRENT_USER_ID)) {
            throw new ResourceNotFoundException("Transaction not found");
        }
        return transaction;
    }

    private BigDecimal convertAmount(BigDecimal amount, CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        return amount.multiply(exchangeRateService.getRate(sourceCurrency, targetCurrency))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private void markSuccess(Transaction transaction, Account account) {
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setAccountVersionAfter(account.getVersion());
    }

    private void markFailed(Transaction transaction, Account account, TransactionStatus status) {
        transaction.setStatus(status);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setAccountVersionAfter(account.getVersion());
        transactionRepository.saveAndFlush(transaction);
    }

    private TransactionResponse retryOnOptimisticLock(Supplier<TransactionResponse> operation) {
        for (int attempt = 0; attempt < MAX_BALANCE_UPDATE_ATTEMPTS; attempt++) {
            try {
                return operation.get();
            } catch (OptimisticLockingFailureException ignored) {
                // Retry with a fresh account version.
            }
        }
        throw new BadRequestException("Account balance changed concurrently. Please retry.");
    }
}
