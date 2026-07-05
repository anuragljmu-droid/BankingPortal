package com.banking_portal.account_management_api.exchange;

import com.banking_portal.account_management_api.account.Account;
import com.banking_portal.account_management_api.account.AccountRepository;
import com.banking_portal.account_management_api.account.AccountService;
import com.banking_portal.account_management_api.common.CurrencyCode;
import com.banking_portal.account_management_api.dto.ExchangeRequest;
import com.banking_portal.account_management_api.dto.TransactionResponse;
import com.banking_portal.account_management_api.exception.BadRequestException;
import com.banking_portal.account_management_api.exception.InsufficientFundsException;
import com.banking_portal.account_management_api.transaction.Transaction;
import com.banking_portal.account_management_api.transaction.TransactionMapper;
import com.banking_portal.account_management_api.transaction.TransactionRepository;
import com.banking_portal.account_management_api.transaction.TransactionStatus;
import com.banking_portal.account_management_api.transaction.TransactionType;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Supplier;

@Service
public class ExchangeService {

    private static final int MAX_BALANCE_UPDATE_ATTEMPTS = 3;

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;
    private final TransactionRepository transactionRepository;

    public ExchangeService(
            AccountRepository accountRepository,
            AccountService accountService,
            ExchangeRateService exchangeRateService,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.exchangeRateService = exchangeRateService;
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> performExchange(ExchangeRequest request) {
        return retryOnOptimisticLock(() -> performExchangeOnce(request));
    }

    @Transactional(noRollbackFor = {BadRequestException.class, InsufficientFundsException.class})
    protected List<TransactionResponse> performExchangeOnce(ExchangeRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BadRequestException("Exchange requires two different accounts");
        }

        Account fromAccount = accountService.getCurrentUserAccount(request.fromAccountId());
        Account toAccount = accountService.getCurrentUserAccount(request.toAccountId());
        BigDecimal targetAmount = convertAmount(request.amount(), fromAccount.getCurrency(), toAccount.getCurrency());

        Transaction outTransaction = transactionRepository.saveAndFlush(new Transaction(
                fromAccount,
                TransactionType.EXCHANGE_OUT,
                TransactionStatus.IN_PROGRESS,
                request.amount(),
                fromAccount.getCurrency(),
                fromAccount.getBalance(),
                request.description()
        ));
        Transaction inTransaction = transactionRepository.saveAndFlush(new Transaction(
                toAccount,
                TransactionType.EXCHANGE_IN,
                TransactionStatus.IN_PROGRESS,
                targetAmount,
                toAccount.getCurrency(),
                toAccount.getBalance(),
                request.description()
        ));
        outTransaction.setRelatedTransactionId(inTransaction.getId());
        inTransaction.setRelatedTransactionId(outTransaction.getId());

        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            markFailed(outTransaction, fromAccount, TransactionStatus.FAILED_INSUFFICIENT_BALANCE);
            markFailed(inTransaction, toAccount, TransactionStatus.FAILED);
            throw new InsufficientFundsException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.amount()));
        toAccount.setBalance(toAccount.getBalance().add(targetAmount));
        Account savedFrom = accountRepository.saveAndFlush(fromAccount);
        Account savedTo = accountRepository.saveAndFlush(toAccount);

        markSuccess(outTransaction, savedFrom);
        markSuccess(inTransaction, savedTo);
        transactionRepository.save(outTransaction);
        transactionRepository.save(inTransaction);

        return List.of(
                TransactionMapper.toResponse(outTransaction),
                TransactionMapper.toResponse(inTransaction)
        );
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

    private List<TransactionResponse> retryOnOptimisticLock(Supplier<List<TransactionResponse>> operation) {
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
