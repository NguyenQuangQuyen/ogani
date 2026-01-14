import { EventEmitter, Injectable } from '@angular/core';
import { InputText } from 'primeng/inputtext';
import { Observable, of, Subject } from 'rxjs';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  items: any[] = [];

  totalPrice = 0;

  total = 0;

  constructor(private messageService: MessageService) { }

  saveCart(): void {
    localStorage.setItem('cart_items', JSON.stringify(this.items));
  }

  addToCart(item: any, quantity: number): boolean {
    this.loadCart();

    // Kiểm tra số lượng tồn kho trước khi thêm vào giỏ hàng
    // item.quantity là stock available, không phải cart quantity
    const availableStock = item.quantity !== undefined ? item.quantity : Infinity;

    // Tính tổng số lượng sau khi thêm
    const existingItem = this.items.find((x: any) => x.id === item.id);
    const currentCartQty = existingItem ? existingItem.quantity : 0;
    const totalQty = currentCartQty + quantity;

    // Kiểm tra nếu vượt quá tồn kho
    if (totalQty > availableStock) {
      const canAdd = availableStock - currentCartQty;
      if (canAdd <= 0) {
        this.messageService.add({
          severity: 'error',
          summary: 'Hết hàng',
          detail: `Sản phẩm "${item.name}" đã đạt số lượng tối đa trong giỏ hàng (${availableStock} sản phẩm)!`,
          life: 4000,
        });
      } else {
        this.messageService.add({
          severity: 'warn',
          summary: 'Không đủ hàng',
          detail: `Chỉ còn ${availableStock} sản phẩm "${item.name}" trong kho. Bạn chỉ có thể thêm ${canAdd} sản phẩm nữa!`,
          life: 4000,
        });
      }
      return false;
    }

    let isNewProduct = false;

    if (!this.productInCart(item)) {
      // Lưu thông tin stock ban đầu vào cartItem
      const cartItem = {
        ...item,
        quantity: quantity,
        subTotal: quantity * item.price,
        availableStock: availableStock // Lưu stock để track
      };
      this.items.push(cartItem);
      isNewProduct = true;
    } else {
      this.items.forEach((res) => {
        if (res.id == item.id) {
          res.quantity += quantity;
          res.subTotal = res.quantity * res.price;
        }
      });
    }

    this.saveCart();
    this.getTotalPrice();

    // Hiển thị thông báo thêm sản phẩm thành công
    if (isNewProduct) {
      this.messageService.add({
        severity: 'success',
        summary: 'Thành công',
        detail: `${item.name} đã được thêm vào giỏ hàng của bạn!`,
        life: 1500,
      });
    } else {
      this.messageService.add({
        severity: 'success',
        summary: 'Thành công',
        detail: `Đã thêm ${quantity} ${item.name} vào giỏ hàng của bạn!`,
        life: 1500,
      });
    }
    return true;
  }

  updateCart(item: any, quantity: number) {
    this.items.forEach((res) => {
      if (res.id == item.id) {
        res.quantity = quantity;
        res.subTotal = res.quantity * res.price;
      }
    });
    this.saveCart();
    this.getTotalPrice();
  }

  productInCart(item: any): boolean {
    return this.items.findIndex((x: any) => x.id == item.id) > -1;
  }
  loadCart(): void {
    this.items = JSON.parse(localStorage.getItem('cart_items') as any) || [];
    this.getTotalPrice();
  }

  getItems() {
    // Make sure cart is loaded before returning items
    if (!this.items || this.items.length === 0) {
      this.loadCart();
    }
    // Ensure we have a proper array
    if (!this.items) {
      this.items = [];
    }
    this.getTotalPrice();
    return this.items;
  }

  getTotalPrice() {
    this.totalPrice = 0;
    this.total = 0;
    this.items.forEach((res) => {
      this.totalPrice += res.subTotal;
      this.total = this.totalPrice;
    });
    return this.totalPrice;
  }

  remove(item: any) {
    const index = this.items.findIndex((o: any) => o.id == item.id);
    if (index > -1) {
      this.items.splice(index, 1);
      this.saveCart();
    }
    this.getTotalPrice();
  }

  clearCart() {
    this.items = [];
    this.getTotalPrice();
    localStorage.removeItem('cart_items');
  }
}
