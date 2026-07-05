package com.banking_portal.account_management_api.account;

import com.banking_portal.account_management_api.dto.AccountResponse;
import com.banking_portal.account_management_api.dto.BalanceResponse;
import com.banking_portal.account_management_api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    public static final long CURRENT_USER_ID = 1L; // Demo user. In production, read this from the authenticated user context.

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getCurrentUserAccounts() {
        return accountRepository.findByUserIdOrderByCreatedAtAsc(CURRENT_USER_ID)
                .stream()
                .map(AccountMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        return AccountMapper.toResponse(getCurrentUserAccount(accountId));
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long accountId) {
        return AccountMapper.toBalanceResponse(getCurrentUserAccount(accountId));
    }

    public Account getCurrentUserAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getId().equals(CURRENT_USER_ID)) {
            throw new ResourceNotFoundException("Account not found");
        }
        return account;
    }
}
