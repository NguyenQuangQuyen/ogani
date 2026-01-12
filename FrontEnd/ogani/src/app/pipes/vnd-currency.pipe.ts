import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'vndCurrency'
})
export class VndCurrencyPipe implements PipeTransform {
  transform(value: number | string | null | undefined): string {
    if (value === null || value === undefined || value === '') {
      return '0 VNĐ';
    }

    // Convert to number if string
    const numValue = typeof value === 'string' ? parseFloat(value) : value;

    if (isNaN(numValue)) {
      return '0 VNĐ';
    }

    // Round to nearest integer (no decimals for VND)
    const roundedValue = Math.round(numValue);

    // Format with dots as thousand separators
    const formattedValue = roundedValue
      .toString()
      .replace(/\B(?=(\d{3})+(?!\d))/g, '.');

    return `${formattedValue} VNĐ`;
  }
}
