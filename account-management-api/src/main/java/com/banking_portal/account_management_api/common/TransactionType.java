package com.banking_portal.account_management_api.common;

public enum TransactionType {
    DEBIT("debit"),
    DEPOSIT("deposit");

    final String name;
    TransactionType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
