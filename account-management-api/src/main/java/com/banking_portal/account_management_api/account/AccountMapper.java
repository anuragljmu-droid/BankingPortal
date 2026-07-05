package com.banking_portal.account_management_api.account;

import com.banking_portal.account_management_api.dto.AccountResponse;
import com.banking_portal.account_management_api.dto.BalanceResponse;

final class AccountMapper {

    private AccountMapper() {
    }

    static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getBalance(),
                account.getVersion(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    static BalanceResponse toBalanceResponse(Account account) {
        return new BalanceResponse(account.getId(), account.getBalance(), account.getCurrency());
    }
}
