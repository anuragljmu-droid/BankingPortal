# Banking Portal

This assignment implements a small banking portal with a Spring Boot REST API and an Angular single-page application.

## Assignment Requirements

Backend requirements:

- Add money to an account.
- Debit money from an account.
- Get account balance.
- Perform currency exchange using fixed exchange rates.
- Retrieve transaction history for a given account.
- Support a user with multiple accounts.
- Keep one currency per account. Supported currencies are `EUR`, `USD`, `SEK`, `GBP`, and `VND`.
- Debit operations must use the account currency only.
- Before debiting, simulate external logging by calling a configured external URL.
- Store data in an SQL database. This project uses H2 for local development.
- Keep the backend service self-contained.

Frontend requirements:

- Show all accounts for the current user.
- Show balance and currency for each account.
- Open an account overview page from the account list.
- Show account balance, currency, transaction history, and balance history chart.
- Load transaction history in pages and load more transactions without a page reload.
- Open a transaction overview page from the transaction list.
- Export a transaction summary as a PDF report.

NgRx is not used. The Angular UI uses local component state and signals.

## Project Structure

```text
.
├── account-management-api      # Spring Boot backend
└── account-management-portal   # Angular frontend
```

## Backend

The backend is implemented in `account-management-api`.

Technology:

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- H2 database
- Maven

The backend runs on port `8081` in the dev profile.

### Backend Behavior

The application currently uses a seeded demo user as the current user. That user has multiple accounts in different currencies.

Accounts:

- Each account belongs to the current demo user.
- Each account has one fixed currency.
- Account balances are stored as `BigDecimal`.
- Account updates use optimistic locking through the account `version` field.

Deposits:

- Deposit requests can include a source currency.
- If the source currency differs from the account currency, the backend converts the amount using the fixed exchange-rate table.
- A transaction is created and marked as `SUCCESS` when the balance update completes.

Debits:

- Debit requests must use the same currency as the account.
- Before the debit is completed, the backend calls the configured external logging URL.
- If the account does not have enough money, the transaction is marked as failed for insufficient balance.
- If external logging fails, the transaction is marked as failed.

Currency exchange:

- Exchanges move money between two accounts owned by the current demo user.
- The source account is debited in its own currency.
- The target account is credited after conversion using the fixed exchange rate.
- The exchange creates paired `EXCHANGE_OUT` and `EXCHANGE_IN` transactions.

Transactions:

- Transaction history is available per account.
- Transaction detail is available by transaction ID.
- Balance history is generated from historical transaction data.
- Transaction statuses include successful, in-progress, and failed states.

External logging:

- The external logging URL is configured in application configuration:


## Backend API

Accounts:

```http
GET /api/accounts
GET /api/accounts/{accountId}
GET /api/accounts/{accountId}/balance
```

Deposit and debit:

```http
POST /api/accounts/{accountId}?action=DEPOSIT
POST /api/accounts/{accountId}?action=DEBIT
```

Request body:

```json
{
  "amount": 100.00,
  "description": "Salary",
  "currency": "EUR"
}
```

Account transactions:

```http
GET /api/accounts/{accountId}/transactions?page=0&size=10&sort=createdAt,desc
```

Balance history:

```http
GET /api/accounts/{accountId}/balance-history
```

Currency exchange:

```http
POST /api/exchanges
```

Request body:

```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 50.00,
  "description": "Exchange to USD"
}
```

Transactions:

```http
GET /api/transactions/{transactionId}
GET /api/transactions/{transactionId}/summary
```

## Frontend

The frontend is implemented in `account-management-portal`.

Technology:

- Angular 21
- Bootstrap 5
- Angular Router
- Angular forms
- Signals/local component state

The frontend runs on port `4200` during local development.

### UI Behavior

Home page:

- Lists all accounts for the current demo user.
- Shows account number, balance, and currency.
- Opens the account overview page when an account is selected.

Account overview page:

- Shows the selected account number, balance, and currency.
- Shows a balance history line chart.
- Provides account actions for deposit, debit, and currency exchange.
- Shows paginated transaction history.
- Loads more transactions dynamically using an infinite-scroll trigger and a manual load-more button.
- Opens the transaction overview page when a transaction is selected.

Transaction overview page:

- Shows transaction reference, date, status, type, amount, balance after transaction, account ID, description, and related transaction ID.
- Provides a button to download a PDF transaction summary.

The account overview UI is split into smaller components:

```text
account-overview
├── account-actions
└── transaction-history
```

### Frontend API Proxy

The Angular dev server proxies API calls to the backend:

```json
{
  "/api": {
    "target": "http://localhost:8081",
    "secure": false,
    "changeOrigin": true
  }
}
```

This lets the UI call `/api/...` while the backend runs on `localhost:8081`.

## Running Locally

Start the backend:

```bash
cd account-management-api
./mvnw spring-boot:run
```

Start the frontend:

```bash
cd account-management-portal
npm install
npm start
```

Open the UI:

```text
http://localhost:4200
```

Backend base URL:

```text
http://localhost:8081
```

## Verification

Backend tests:

```bash
cd account-management-api
./mvnw test
```

Frontend build/check:

```bash
cd account-management-portal
npm run build
```
