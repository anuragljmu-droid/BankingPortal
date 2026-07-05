# BankingPortal

Interview assignment for a banking portal with a Spring Boot backend and an Angular frontend.

## Assignment Summary

Backend requirements:

- Add money to an account.
- Debit money from an account.
- Get account balance.
- Perform currency exchange with fixed exchange rates.
- Retrieve transaction history for an account.
- Support users with multiple accounts.
- Each account has exactly one currency: `EUR`, `USD`, `SEK`, `GBP`, `VND`.
- Debit operations use the account currency only.
- Before debiting, call an external page to simulate external logging.
- Store data in SQL. This project currently uses H2.

Frontend requirements will be handled later in `account-management-portal`.

## Project Structure

```text
.
├── account-management-api
└── account-management-portal
```

Current backend package:

```text
com.banking_portal.account_management_api
├── account
├── common
├── exchange
├── transaction
└── user
```

## Backend Current State

The backend project is `account-management-api`.

Implemented so far:

- Spring Boot application setup.
- JPA entities and repositories.
- H2 dev database configuration.
- Dev-profile SQL seed data.
- JPA auditing for created/updated timestamps.
- Optimistic locking on accounts.
- Transaction statuses.
- Fixed exchange-rate table.

## Domain Model

### User

Represents a banking customer.

Fields:

- `id`
- `firstName`
- `lastName`
- `email`
- `createdAt`
- `updatedAt`

Relationships:

- One user can have many accounts.

Auditing:

- `createdAt` uses `@CreatedDate`.
- `updatedAt` uses `@LastModifiedDate`.

### Account

Represents one bank account with one fixed currency.

Fields:

- `id`
- `user`
- `accountNumber`
- `currency`
- `balance`
- `version`
- `createdAt`
- `updatedAt`

Important details:

- `balance` uses `BigDecimal`.
- `currency` is one of `EUR`, `USD`, `SEK`, `GBP`, `VND`.
- `version` uses JPA optimistic locking with `@Version`.
- Optimistic locking protects balance updates from lost concurrent writes.

### Transaction

Represents a balance-affecting operation.

Fields:

- `id`
- `account`
- `type`
- `status`
- `amount`
- `currency`
- `balanceAfter`
- `accountVersionAfter`
- `description`
- `createdAt`
- `reference`
- `relatedTransactionId`

Transaction types:

- `DEPOSIT`
- `DEBIT`
- `EXCHANGE_IN`
- `EXCHANGE_OUT`

Transaction statuses:

- `IN_PROGRESS`
- `SUCCESS`
- `FAILED`

Lifecycle:

```text
IN_PROGRESS -> SUCCESS
IN_PROGRESS -> FAILED
```

`SUCCESS` and `FAILED` are terminal states by convention. The service layer will enforce this.

`accountVersionAfter` is a snapshot of the account version after the transaction completes. It is not modeled as a foreign key because the JPA account version is not a separate table row.

### ExchangeRate

Stores fixed exchange rates in SQL.

Fields:

- `id`
- `sourceCurrency`
- `targetCurrency`
- `rate`
- `createdAt`
- `updatedAt`

The table has a unique constraint on:

```text
source_currency + target_currency
```

## Seed Data

Seed data is loaded from:

```text
account-management-api/src/main/resources/data-dev.sql
```

It is enabled only through the dev profile configuration in:

```text
account-management-api/src/main/resources/application-dev.yml
```

Current seed data includes:

- Demo user with ID `1`.
- Accounts for `EUR`, `USD`, `SEK`, `GBP`, and `VND`.
- Sample successful transactions.
- Fixed exchange rates between supported currencies.

The app currently treats user ID `1` as the demo/current user. Later, this should come from the authentication context after login.

## Planned Backend API

Accounts:

```http
GET /api/accounts
GET /api/accounts/{accountId}
```

Deposits and debits:

```http
POST /api/accounts/{accountId}?action=deposit
POST /api/accounts/{accountId}?action=debit
```

Example deposit body:

```json
{
  "amount": 100.00,
  "description": "Salary",
  "currency": "SEK"
}
```

Transaction history:

```http
GET /api/accounts/{accountId}/transactions?page=0&size=20
GET /api/transactions/{transactionId}
```

Balance history:

```http
GET /api/accounts/{accountId}/balance-history
```

Transaction summary:

```http
GET /api/transactions/{transactionId}/summary
```

## Planned Backend Services

- `AccountService`
- `TransactionService`
- `ExchangeService`
- `ExchangeRateService`
- `ExternalLoggingService`

Debit flow will call an external logging URL before completing the debit.

Balance-changing flows will:

- create a transaction as `IN_PROGRESS`;
- update account balance;
- store `balanceAfter` and `accountVersionAfter`;
- mark the transaction as `SUCCESS`;
- mark the transaction as `FAILED` if the operation cannot complete.

Account updates should retry a small number of times when optimistic locking detects a concurrent modification.

## Run Backend

From the backend folder:

```bash
cd account-management-api
./mvnw spring-boot:run
```

The dev server runs on:

```text
http://localhost:8081
```

H2 console:

```text
http://localhost:8081/h2-console
```

Dev JDBC URL:

```text
jdbc:h2:mem:testdb
```

## Run Tests

From the backend folder:

```bash
cd account-management-api
./mvnw test
```

Current verification:

```text
./mvnw clean test
```

passes successfully.
