import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { Transaction } from '../shared/bank.models';
import { BankApiService } from '../shared/bank-api.service';
import { dateTime, money, transactionLabel } from '../shared/formatters';
import { PdfExportService } from '../shared/pdf-export.service';

@Component({
  selector: 'app-transaction-overview',
  imports: [RouterLink],
  templateUrl: './transaction-overview.component.html',
})
export class TransactionOverviewComponent implements OnInit {
  readonly transaction = signal<Transaction | null>(null);
  readonly isLoading = signal(true);
  readonly isExporting = signal(false);
  readonly error = signal<string | null>(null);

  readonly money = money;
  readonly dateTime = dateTime;
  readonly transactionLabel = transactionLabel;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bankApi: BankApiService,
    private readonly pdfExport: PdfExportService,
  ) {}

  ngOnInit(): void {
    const transactionId = Number(this.route.snapshot.paramMap.get('transactionId'));
    this.bankApi.getTransaction(transactionId).subscribe({
      next: (transaction) => {
        this.transaction.set(transaction);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Transaction could not be loaded.');
        this.isLoading.set(false);
      },
    });
  }

  exportPdf(): void {
    const transaction = this.transaction();
    if (!transaction) {
      return;
    }

    this.isExporting.set(true);
    this.bankApi.getTransactionSummary(transaction.id).subscribe({
      next: (summary) => {
        this.pdfExport.downloadTransactionSummary(summary);
        this.isExporting.set(false);
      },
      error: () => {
        this.error.set('Transaction summary could not be exported.');
        this.isExporting.set(false);
      },
    });
  }
}
