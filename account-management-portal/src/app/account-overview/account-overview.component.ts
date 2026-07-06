import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import {
  Account,
  BalanceHistoryPoint,
  CurrencyCode,
  Transaction,
} from '../shared/bank.models';
import { BankApiService } from '../shared/bank-api.service';
import { money } from '../shared/formatters';
import { AccountActionsComponent, ExchangeForm, MoneyMovementForm } from './account-actions/account-actions.component';
import { TransactionHistoryComponent } from './transaction-history/transaction-history.component';

@Component({
  selector: 'app-account-overview',
  imports: [AccountActionsComponent, RouterLink, TransactionHistoryComponent],
  templateUrl: './account-overview.component.html',
  styleUrl: './account-overview.component.css',
})
export class AccountOverviewComponent implements OnInit {
  readonly account = signal<Account | null>(null);
  readonly accounts = signal<Account[]>([]);
  readonly transactions = signal<Transaction[]>([]);
  readonly history = signal<BalanceHistoryPoint[]>([]);
  readonly isLoading = signal(true);
  readonly isLoadingMore = signal(false);
  readonly isSubmitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly hasMore = signal(true);

  readonly money = money;

  private accountId = 0;
  private page = 0;
  private readonly pageSize = 10;

  readonly chartPolyline = computed(() => {
    const points = this.history()
      .filter((point) => !point.status?.startsWith('FAILED'))
      .slice()
      .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

    if (!points.length) {
      return '';
    }

    if (points.length === 1) {
      return '24,110 616,110';
    }

    const balances = points.map((point) => Number(point.balance));
    const min = Math.min(...balances);
    const max = Math.max(...balances);
    const range = max - min || 1;
    const left = 24;
    const width = 592;
    const top = 18;
    const height = 174;

    return points
      .map((point, index) => {
        const x = left + (index / (points.length - 1)) * width;
        const y = top + (1 - (Number(point.balance) - min) / range) * height;
        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  });

  readonly chartMinLabel = computed(() => {
    const account = this.account();
    const balances = this.history().map((point) => Number(point.balance));
    return account && balances.length ? money(Math.min(...balances), account.currency) : '';
  });

  readonly chartMaxLabel = computed(() => {
    const account = this.account();
    const balances = this.history().map((point) => Number(point.balance));
    return account && balances.length ? money(Math.max(...balances), account.currency) : '';
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bankApi: BankApiService,
  ) {}

  ngOnInit(): void {
    this.accountId = Number(this.route.snapshot.paramMap.get('accountId'));
    this.loadAccountScreen();
  }

  loadMoreTransactions(): void {
    if (!this.hasMore() || this.isLoadingMore() || this.isLoading()) {
      return;
    }

    this.isLoadingMore.set(true);
    this.bankApi.getTransactions(this.accountId, this.page, this.pageSize).subscribe({
      next: (page) => {
        this.transactions.update((existing) => [...existing, ...page.content]);
        this.hasMore.set(!page.last);
        this.page += 1;
        this.isLoadingMore.set(false);
      },
      error: () => {
        this.error.set('More transactions could not be loaded.');
        this.isLoadingMore.set(false);
      },
    });
  }

  submitMovement(form: MoneyMovementForm): void {
    const account = this.account();
    if (!account) {
      return;
    }

    this.isSubmitting.set(true);
    this.clearMessages();
    this.bankApi
      .moveMoney(account.id, form.action, {
        amount: form.amount,
        description: form.description || `${form.action} from portal`,
        currency: account.currency as CurrencyCode,
      })
      .subscribe({
        next: () => {
          this.success.set('Money movement completed.');
          this.reloadAfterAction();
        },
        error: () => {
          this.error.set('Money movement failed. Check amount, currency, and available balance.');
          this.isSubmitting.set(false);
        },
      });
  }

  submitExchange(form: ExchangeForm): void {
    const account = this.account();
    if (!account) {
      return;
    }

    this.isSubmitting.set(true);
    this.clearMessages();
    this.bankApi
      .performExchange({
        fromAccountId: account.id,
        toAccountId: form.toAccountId,
        amount: form.amount,
        description: form.description || 'Exchange from portal',
      })
      .subscribe({
        next: () => {
          this.success.set('Currency exchange completed.');
          this.reloadAfterAction();
        },
        error: () => {
          this.error.set('Currency exchange failed. Check the selected accounts and amount.');
          this.isSubmitting.set(false);
        },
      });
  }

  private loadAccountScreen(): void {
    this.isLoading.set(true);
    this.clearMessages();
    forkJoin({
      account: this.bankApi.getAccount(this.accountId),
      accounts: this.bankApi.getAccounts(),
      history: this.bankApi.getBalanceHistory(this.accountId),
    }).subscribe({
      next: ({ account, accounts, history }) => {
        this.account.set(account);
        this.accounts.set(accounts);
        this.history.set(history);
        this.transactions.set([]);
        this.page = 0;
        this.hasMore.set(true);
        this.isLoading.set(false);
        this.loadMoreTransactions();
      },
      error: () => {
        this.error.set('Account details could not be loaded.');
        this.isLoading.set(false);
      },
    });
  }

  private reloadAfterAction(): void {
    forkJoin({
      account: this.bankApi.getAccount(this.accountId),
      history: this.bankApi.getBalanceHistory(this.accountId),
    }).subscribe({
      next: ({ account, history }) => {
        this.account.set(account);
        this.history.set(history);
        this.transactions.set([]);
        this.page = 0;
        this.hasMore.set(true);
        this.isSubmitting.set(false);
        this.loadMoreTransactions();
      },
      error: () => {
        this.error.set('The action completed, but the updated account could not be loaded.');
        this.isSubmitting.set(false);
      },
    });
  }

  private clearMessages(): void {
    this.error.set(null);
    this.success.set(null);
  }
}
