import { Injectable } from '@angular/core';
import { Transaction } from './bank.models';
import { dateTime, money, transactionLabel } from './formatters';

@Injectable({ providedIn: 'root' })
export class PdfExportService {
  downloadTransactionSummary(transaction: Transaction): void {
    const rows = [
      ['Transaction ID', String(transaction.id)],
      ['Reference', transaction.reference],
      ['Account ID', String(transaction.accountId)],
      ['Type', transactionLabel(transaction.type)],
      ['Status', transaction.status],
      ['Amount', money(transaction.amount, transaction.currency)],
      ['Balance after', money(transaction.balanceAfter, transaction.currency)],
      ['Created at', dateTime(transaction.createdAt)],
      ['Description', transaction.description || ''],
      ['Related transaction', transaction.relatedTransactionId ? String(transaction.relatedTransactionId) : ''],
    ];

    const lines = ['Transaction Summary', '', ...rows.map(([label, value]) => `${label}: ${value}`)];
    const pdf = this.createSimplePdf(lines);
    const url = URL.createObjectURL(new Blob([pdf], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = `transaction-${transaction.id}-summary.pdf`;
    link.click();
    URL.revokeObjectURL(url);
  }

  private createSimplePdf(lines: string[]): ArrayBuffer {
    const escapedLines = lines.map((line) => this.escapePdfText(line));
    const textCommands = escapedLines
      .map((line, index) => `/F1 ${index === 0 ? '18' : '12'} Tf 1 0 0 1 72 ${760 - index * 24} Tm (${line}) Tj`)
      .join('\n');
    const stream = `BT\n${textCommands}\nET`;

    const objects = [
      '<< /Type /Catalog /Pages 2 0 R >>',
      '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
      '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>',
      '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>',
      `<< /Length ${stream.length} >>\nstream\n${stream}\nendstream`,
    ];

    let pdf = '%PDF-1.4\n';
    const offsets = [0];
    objects.forEach((object, index) => {
      offsets.push(pdf.length);
      pdf += `${index + 1} 0 obj\n${object}\nendobj\n`;
    });

    const xrefOffset = pdf.length;
    pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
    offsets.slice(1).forEach((offset) => {
      pdf += `${String(offset).padStart(10, '0')} 00000 n \n`;
    });
    pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF`;

    const bytes = new TextEncoder().encode(pdf);
    return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
  }

  private escapePdfText(value: string): string {
    return value
      .normalize('NFKD')
      .replace(/[^\x20-\x7E]/g, '')
      .replace(/\\/g, '\\\\')
      .replace(/\(/g, '\\(')
      .replace(/\)/g, '\\)');
  }
}
