import { EventEmitter, Injectable } from '@angular/core';
import { InputText } from 'primeng/inputtext';
import { Observable, of, Subject } from 'rxjs';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  items: any[] = [];

  totalPrice = 0;

  total = 0;

  constructor(
    private messageService: MessageService
  ) { }


  saveCart(): void {
    localStorage.setItem('cart_items', JSON.stringify(this.items));
  }

  addToCart(item: any, quantity: number) {
    this.loadCart();
    let isNewProduct = false;

    if (!this.productInCart(item)) {
      item.quantity = quantity;
      item.subTotal = item.quantity * item.price;
      this.items.push(item);
      isNewProduct = true;
    } else {
      this.items.forEach(res => {
        if (res.id == item.id) {
          res.quantity += quantity;
          res.subTotal = res.quantity * res.price;
        }
      });
    }

    item.quantity = quantity;
    this.saveCart();
    this.getTotalPrice();

    // Hiển thị thông báo thêm sản phẩm thành công bằng tiếng Anh
    if (isNewProduct) {
      this.messageService.add({
        severity: 'success',
        summary: 'Success',
        detail: `${item.name} has been added to your cart!`,
        life: 3000
      });
    } else {
      this.messageService.add({
        severity: 'success',
        summary: 'Success',
        detail: `Added ${quantity} more ${item.name} to your cart!`,
        life: 3000
      });
    }
  }


  updateCart(item: any, quantity: number) {
    this.items.forEach(res => {
      if (res.id == item.id) {
        res.quantity = quantity;
        res.subTotal = res.quantity * res.price;
      }
    })
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
    this.items.forEach(res => {
      this.totalPrice += res.subTotal;
      this.total = this.totalPrice;
    })
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
