package com.banking_portal.account_management_api.transaction;

import com.banking_portal.account_management_api.account.Account;
import com.banking_portal.account_management_api.common.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.IN_PROGRESS;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    private Long accountVersionAfter;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, unique = true, updatable = false)
    private String reference;

    private Long relatedTransactionId;

    public Transaction(
            Account account,
            TransactionType type,
            BigDecimal amount,
            CurrencyCode currency,
            BigDecimal balanceAfter,
            String description
    ) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public Transaction(
            Account account,
            TransactionType type,
            TransactionStatus status,
            BigDecimal amount,
            CurrencyCode currency,
            BigDecimal balanceAfter,
            String description
    ) {
        this(account, type, amount, currency, balanceAfter, description);
        this.status = status;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (status == null) {
            status = TransactionStatus.IN_PROGRESS;
        }
        if (reference == null) {
            reference = "TXN-" + UUID.randomUUID();
        }
    }
}
