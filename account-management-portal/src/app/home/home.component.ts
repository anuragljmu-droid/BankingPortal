import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Account } from '../shared/bank.models';
import { BankApiService } from '../shared/bank-api.service';
import { money } from '../shared/formatters';
import { single } from 'rxjs';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  readonly accounts = signal<Account[]>([]);
  readonly isLoading = signal(true);
  readonly error = signal<string | null>(null);
  readonly username = signal<string>('John Doe'); // In a real application, this would be fetched from the token provided by the backend

  readonly money = money;

  constructor(private readonly bankApi: BankApiService) {}

  ngOnInit(): void {
    this.bankApi.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Accounts could not be loaded. Check that the backend is running.');
        this.isLoading.set(false);
      },
    });
  }
}
