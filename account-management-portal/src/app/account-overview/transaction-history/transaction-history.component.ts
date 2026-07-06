import { Component, ElementRef, EventEmitter, Input, OnDestroy, Output, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Transaction } from '../../shared/bank.models';
import { dateTime, money, transactionLabel } from '../../shared/formatters';

@Component({
  selector: 'app-transaction-history',
  imports: [RouterLink],
  templateUrl: './transaction-history.component.html',
  styleUrl: './transaction-history.component.css',
})
export class TransactionHistoryComponent implements OnDestroy {
  @Input({ required: true }) transactions: Transaction[] = [];
  @Input() isLoadingMore = false;
  @Input() hasMore = false;

  @Output() loadMore = new EventEmitter<void>();

  readonly money = money;
  readonly dateTime = dateTime;
  readonly transactionLabel = transactionLabel;

  private observer: IntersectionObserver | null = null;

  @ViewChild('loadMoreAnchor')
  set loadMoreAnchor(anchor: ElementRef<HTMLElement> | undefined) {
    this.observer?.disconnect();
    if (!anchor || typeof IntersectionObserver === 'undefined') {
      return;
    }

    this.observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        this.loadMore.emit();
      }
    });
    this.observer.observe(anchor.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
