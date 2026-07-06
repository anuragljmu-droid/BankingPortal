export type CurrencyCode = 'EUR' | 'USD' | 'SEK' | 'GBP' | 'VND';

export type MoneyMovementAction = 'DEPOSIT' | 'DEBIT';

export type TransactionType = MoneyMovementAction | 'EXCHANGE_IN' | 'EXCHANGE_OUT' | string;

export type TransactionStatus = 'SUCCESS' | 'FAILED' | 'FAILED_INSUFFICIENT_BALANCE' | 'IN_PROGRESS';

export interface Account {
  id: number;
  accountNumber: string;
  currency: CurrencyCode;
  balance: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface BalanceHistoryPoint {
  timestamp: string;
  balance: number;
  status: TransactionStatus;
}

export interface MoneyMovementRequest {
  amount: number;
  description?: string;
  currency: CurrencyCode;
}

export interface ExchangeRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  description?: string;
}

export interface Transaction {
  id: number;
  accountId: number;
  type: TransactionType;
  status: TransactionStatus;
  amount: number;
  currency: CurrencyCode;
  balanceAfter: number;
  accountVersionAfter: number;
  description: string | null;
  createdAt: string;
  reference: string;
  relatedTransactionId: number | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}
