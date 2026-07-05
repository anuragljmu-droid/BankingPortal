import { Routes } from '@angular/router';
import { AccountOverviewComponent } from './account-overview/account-overview.component';
import { HomeComponent } from './home/home.component';
import { TransactionOverviewComponent } from './transaction-overview/transaction-overview.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'Accounts' },
  { path: 'accounts/:accountId', component: AccountOverviewComponent, title: 'Account overview' },
  { path: 'transactions/:transactionId', component: TransactionOverviewComponent, title: 'Transaction overview' },
  { path: '**', redirectTo: '' },
];
