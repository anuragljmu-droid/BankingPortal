import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Account, MoneyMovementAction } from '../../shared/bank.models';

export interface MoneyMovementForm {
  action: MoneyMovementAction;
  amount: number;
  description: string;
}

export interface ExchangeForm {
  toAccountId: number;
  amount: number;
  description: string;
}

@Component({
  selector: 'app-account-actions',
  imports: [FormsModule],
  templateUrl: './account-actions.component.html',
})
export class AccountActionsComponent {
  @Input({ required: true }) account!: Account;
  @Input({ required: true }) accounts: Account[] = [];
  @Input() isSubmitting = false;

  @Output() movementSubmit = new EventEmitter<MoneyMovementForm>();
  @Output() exchangeSubmit = new EventEmitter<ExchangeForm>();

  movementAction: MoneyMovementAction = 'DEPOSIT';
  movementAmount = 25;
  movementDescription = '';
  exchangeToAccountId: number | null = null;
  exchangeAmount = 10;
  exchangeDescription = '';

  submitMovement(): void {
    this.movementSubmit.emit({
      action: this.movementAction,
      amount: this.movementAmount,
      description: this.movementDescription,
    });
  }

  submitExchange(): void {
    const toAccountId = this.exchangeToAccountId ?? this.defaultExchangeTarget()?.id;
    if (!toAccountId) {
      return;
    }

    this.exchangeSubmit.emit({
      toAccountId,
      amount: this.exchangeAmount,
      description: this.exchangeDescription,
    });
  }

  defaultExchangeTarget(): Account | undefined {
    return this.accounts.find((target) => target.id !== this.account.id);
  }
}
