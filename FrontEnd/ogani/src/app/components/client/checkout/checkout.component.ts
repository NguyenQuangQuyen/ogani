import { Component, OnInit } from '@angular/core';
import {
  faBars,
  faHeart,
  faPhone,
  faShoppingBag,
} from '@fortawesome/free-solid-svg-icons';
import { MessageService } from 'primeng/api';
import { Order } from 'src/app/_class/order';
import { OrderDetail } from 'src/app/_class/order-detail';
import { CartService } from 'src/app/_service/cart.service';
import { OrderService } from 'src/app/_service/order.service';
import { StorageService } from 'src/app/_service/storage.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css'],
  providers: [MessageService],
})
export class CheckoutComponent implements OnInit {
  heart = faHeart;
  bag = faShoppingBag;
  phone = faPhone;
  bars = faBars;
  showDepartment = false;
  order = new Order();
  listOrderDetail: any[] = [];
  username!: string;
  userId!: string;
  orderSuccess = false;
  placedOrder: any = null;

  total: number = 0;

  // Các biến cho PayOS
  payosCheckoutUrl: string = '';
  isProcessingPayOS: boolean = false;

  orderForm: any = {
    firstname: null,
    lastname: null,
    country: null,
    address: null,
    town: null,
    state: null,
    email: null,
    phone: null,
    note: null,
    paymentMethod: null,
  };

  constructor(
    public cartService: CartService,
    private orderService: OrderService,
    private storageService: StorageService,
    private messageService: MessageService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Get current user
    const user = this.storageService.getUser();
    this.username = user.username;

    if (user) {
      if (user.id !== undefined && user.id !== null) {
        this.userId = String(user.id);
      } else if (user.userId !== undefined && user.userId !== null) {
        this.userId = String(user.userId);
      } else {
        console.warn('UserId not found in user object, using username as fallback');
        this.userId = this.username;
      }
    } else {
      console.error('User not found in storage');
      this.userId = '';
    }

    // Ensure cart is loaded and total is calculated
    this.cartService.loadCart();
    this.cartService.getTotalPrice();

    // Get total from cart service
    this.total = this.cartService.total;
    console.log('Cart total from service:', this.total);

    if (this.total <= 0 || isNaN(this.total)) {
      console.error('Invalid total value:', this.total);
      this.total = 1;
    } else {
      this.total = parseFloat(this.total.toFixed(2));
    }

    console.log('Final total for checkout:', this.total);

    // Set default orderForm values
    this.orderForm = {
      ...this.orderForm,
      paymentMethod: 'COD',
    };
  }

  showDepartmentClick() {
    this.showDepartment = !this.showDepartment;
  }

  onPaymentMethodChange(method: string, event: any) {
    const checked = event.target.checked;

    if (method === 'COD') {
      if (checked) {
        this.orderForm.paymentMethod = 'COD';
      } else {
        this.orderForm.paymentMethod = null;
      }
    } else if (method === 'BANK') {
      if (checked) {
        this.orderForm.paymentMethod = 'BANK';
      } else {
        this.orderForm.paymentMethod = null;
      }
    }
  }

  selectPaymentMethod(method: string) {
    if (this.orderForm.paymentMethod === method) {
      this.orderForm.paymentMethod = null;
    } else {
      this.orderForm.paymentMethod = method;
    }
  }

  // Xử lý thanh toán bằng PayOS (Bank Transfer)
  placeOrderWithPayOS() {
    if (this.isProcessingPayOS) {
      return;
    }

    if (!this.validateForm()) {
      this.orderForm.paymentMethod = null;
      return;
    }

    if (!this.userId || this.userId === '') {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'User information is missing. Please log in again.',
      });
      this.orderForm.paymentMethod = null;
      return;
    }

    this.isProcessingPayOS = true;

    const {
      firstname,
      lastname,
      country,
      address,
      town,
      state,
      phone,
      email,
      note,
    } = this.orderForm;

    const cartItems = this.cartService.getItems();
    if (!cartItems || cartItems.length === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Your cart is empty. Please add products to your cart before checkout.',
      });
      return;
    }

    const productNames = cartItems.map((item) => item.name).join(', ');
    const productName = productNames.length > 200
      ? productNames.substring(0, 200) + '...'
      : productNames;

    const productId = cartItems.length > 0 && cartItems[0].id ? cartItems[0].id : null;
    const description = `${this.username} - ${productName}`;

    const baseUrl = window.location.origin;
    const returnUrl = `${baseUrl}/payos-return`;
    const cancelUrl = `${baseUrl}/checkout`;

    const listOrderDetail = cartItems.map((item) => {
      return new OrderDetail(item.name, parseInt(String(item.price)), item.quantity, item.id);
    });

    this.orderService
      .createOrderWithPayOS(
        this.userId,
        this.username,
        firstname,
        lastname,
        country,
        address,
        state,
        phone,
        note || '',
        town || '',
        '',
        email,
        productName,
        description,
        returnUrl,
        cancelUrl,
        this.total,
        productId,
        listOrderDetail,
        this.orderForm.paymentMethod || 'BANK'
      )
      .subscribe({
        next: (res) => {
          console.log('PayOS order response:', res);
          const responseData = res.data || res;
          const checkoutUrl = responseData.checkoutUrl;
          const link = responseData.link;

          if (checkoutUrl) {
            console.log('Redirecting to PayOS checkout URL:', checkoutUrl);
            this.messageService.add({
              severity: 'success',
              summary: 'Redirecting',
              detail: 'Redirecting to payment page...',
              life: 1500,
            });
            setTimeout(() => {
              window.location.href = checkoutUrl;
            }, 500);
          } else if (link) {
            console.log('Redirecting to PayOS link:', link);
            this.messageService.add({
              severity: 'success',
              summary: 'Redirecting',
              detail: 'Redirecting to payment page...',
              life: 1500,
            });
            setTimeout(() => {
              window.location.href = link;
            }, 500);
          } else {
            console.error('PayOS response does not contain checkoutUrl or link:', res);
            this.isProcessingPayOS = false;
            this.orderForm.paymentMethod = null;
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Invalid response from payment service. Please try again.',
            });
          }
        },
        error: (err) => {
          console.error('Error creating PayOS order:', err);
          this.isProcessingPayOS = false;
          this.orderForm.paymentMethod = null;
          let errorMessage = 'An error occurred while creating the order with PayOS.';
          if (err.error && err.error.message) {
            errorMessage += ' Details: ' + err.error.message;
          }

          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMessage,
          });
        },
      });
  }

  placeOrder() {
    if (!this.validateForm()) {
      return;
    }

    if (this.orderForm.paymentMethod === 'BANK') {
      this.placeOrderWithPayOS();
      return;
    }

    // Logic xử lý đơn hàng COD (Thanh toán khi nhận hàng)
    const {
      firstname,
      lastname,
      country,
      address,
      town,
      state,
      phone,
      email,
      note,
    } = this.orderForm;

    const cartItems = this.cartService.getItems();
    if (!cartItems || cartItems.length === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Your cart is empty. Please add products to your cart before checkout.',
      });
      return;
    }

    this.listOrderDetail = cartItems.map((item) => {
      return new OrderDetail(item.name, parseInt(String(item.price)), item.quantity, item.id);
    });

    if (!this.listOrderDetail || this.listOrderDetail.length === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Order details list cannot be empty. Please refresh and try again.',
      });
      return;
    }

    // Debug: Log order details để verify productId
    console.log('=== DEBUG: Cart items ===', cartItems);
    console.log('=== DEBUG: listOrderDetail with productId ===', this.listOrderDetail);
    this.listOrderDetail.forEach((item, index) => {
      console.log(`Item ${index}: name=${item.name}, productId=${item.productId}, qty=${item.quantity}`);
    });

    this.orderService
      .placeOrder(
        firstname,
        lastname,
        country,
        address,
        town,
        state,
        '',
        phone,
        email,
        note,
        this.listOrderDetail,
        this.username,
        this.userId,
        this.orderForm.paymentMethod || 'COD'
      )
      .subscribe({
        next: (res) => {
          this.placedOrder = res;
          this.orderSuccess = true;

          const backendOrderId = (res && (res.orderId || (res.data && res.data.orderId))) ? String(res.orderId || res.data.orderId) : String(Date.now());
          const order = {
            firstname,
            lastname,
            country,
            address,
            town,
            state,
            phone,
            email,
            note,
            totalPrice: this.total,
            orderDetails: this.listOrderDetail,
            username: this.username,
            createdDate: new Date(),
            paymentMethod: this.orderForm.paymentMethod || 'COD',
            status: 'Unpaid',
            id: parseInt(backendOrderId)
          };
          this.saveOrderToLocalStorage(order);

          this.cartService.clearCart();

          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Đơn hàng đã được đặt thành công!',
          });

          setTimeout(() => {
            this.router.navigate(['/my-order']);
          }, 2000);
        },
        error: (err) => {
          console.error('Error placing order:', err);
          let errorMessage = 'An error occurred while placing the order.';
          if (err.error && err.error.message) {
            errorMessage += ' Details: ' + err.error.message;
          }

          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMessage,
          });
        },
      });
  }

  saveOrderToLocalStorage(order: any) {
    const ordersJson = localStorage.getItem(`orders_${this.username}`);
    let orders: any[] = [];

    if (ordersJson) {
      try {
        orders = JSON.parse(ordersJson);
      } catch (e) {
        console.error('Error parsing orders from localStorage:', e);
        orders = [];
      }
    }

    if (!order.id) {
      order.id = Date.now();
    }

    orders.push(order);
    localStorage.setItem(`orders_${this.username}`, JSON.stringify(orders));

    const allOrdersJson = localStorage.getItem('orders_all');
    let allOrders: any[] = [];

    if (allOrdersJson) {
      try {
        allOrders = JSON.parse(allOrdersJson);
      } catch (e) {
        allOrders = [];
      }
    }

    allOrders.push(order);
    localStorage.setItem('orders_all', JSON.stringify(allOrders));
    localStorage.setItem(`order_${order.id}`, JSON.stringify(order));
  }

  validateForm() {
    const requiredFields = [
      'firstname',
      'lastname',
      'country',
      'address',
      'town',
      'state',
      'email',
      'phone',
    ];

    for (const field of requiredFields) {
      if (!this.orderForm[field]) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: `Please fill in all required fields`,
        });
        return false;
      }
    }

    const phoneRegex = /^\d{10,15}$/;
    if (!phoneRegex.test(this.orderForm.phone)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Phone number must be between 10 and 15 digits',
      });
      return false;
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    if (!emailRegex.test(this.orderForm.email)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Email must be in the correct format and end with @gmail.com',
      });
      return false;
    }

    return true;
  }
}
