import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  Account,
  BalanceHistoryPoint,
  ExchangeRequest,
  MoneyMovementRequest,
  Page,
  Transaction,
} from './bank.models';

@Injectable({ providedIn: 'root' })
export class BankApiService {
  private readonly baseUrl = '/api';

  constructor(private readonly http: HttpClient) {}

  getAccounts() {
    return this.http.get<Account[]>(`${this.baseUrl}/accounts`);
  }

  getAccount(accountId: number) {
    return this.http.get<Account>(`${this.baseUrl}/accounts/${accountId}`);
  }

  moveMoney(accountId: number, action: 'deposit' | 'debit', request: MoneyMovementRequest) {
    return this.http.post<Transaction>(`${this.baseUrl}/accounts/${accountId}`, request, {
      params: { action },
    });
  }

  performExchange(request: ExchangeRequest) {
    return this.http.post<Transaction[]>(`${this.baseUrl}/exchanges`, request);
  }

  getTransactions(accountId: number, page: number, size = 10) {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');

    return this.http.get<Page<Transaction>>(`${this.baseUrl}/accounts/${accountId}/transactions`, {
      params,
    });
  }

  getBalanceHistory(accountId: number) {
    return this.http.get<BalanceHistoryPoint[]>(`${this.baseUrl}/accounts/${accountId}/balance-history`);
  }

  getTransaction(transactionId: number) {
    return this.http.get<Transaction>(`${this.baseUrl}/transactions/${transactionId}`);
  }

  getTransactionSummary(transactionId: number) {
    return this.http.get<Transaction>(`${this.baseUrl}/transactions/${transactionId}/summary`);
  }

  // getCurrentUser() {
  //   return "John Doe"; // In a real application, this would be fetched from the token provided by the backend
  // }
}
