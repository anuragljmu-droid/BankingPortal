package com.banking_portal.account_management_api.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);
}
