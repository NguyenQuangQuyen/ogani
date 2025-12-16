import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { faBars, faHeart, faPhone, faShoppingBag } from '@fortawesome/free-solid-svg-icons';
import { MessageService } from 'primeng/api';
import { Order } from 'src/app/_class/order';
import { OrderDetail } from 'src/app/_class/order-detail';

import { CartService } from 'src/app/_service/cart.service';
import { OrderService } from 'src/app/_service/order.service';
import { StorageService } from 'src/app/_service/storage.service';
import { Router } from '@angular/router';
import { HttpHeaders } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

declare var paypal: any;

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css'],
  providers: [MessageService]

})
export class CheckoutComponent implements OnInit {
  heart = faHeart;
  bag = faShoppingBag;
  phone = faPhone;
  bars = faBars;
  showDepartment = false;
  order = new Order();
  listOrderDetail: any[] = [];
  username !: string;
  orderSuccess = false;
  placedOrder: any = null;

  // Thêm hai biến sau
  total: number = 0;
  paypalOrderId?: string;
  qrPaymentUrl: string = '';
  @ViewChild('vnpayQRModal') vnpayQRModal!: TemplateRef<any>;
  orderPendingData: any = null;
  private ngbModalRef: any;

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
    paymentMethod: null
  }

  constructor(
    public cartService: CartService,
    private orderService: OrderService,
    private storageService: StorageService,
    private messageService: MessageService,
    private router: Router,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    // Get current user
    this.username = this.storageService.getUser().username;

    // Ensure cart is loaded and total is calculated
    this.cartService.loadCart();
    this.cartService.getTotalPrice();

    // Get total from cart service
    this.total = this.cartService.total;

    console.log('Cart total from service:', this.total);

    // Ensure total is a valid number with 2 decimal places
    if (this.total <= 0 || isNaN(this.total)) {
      console.error('Invalid total value:', this.total);
      this.total = 1; // Giá trị mặc định tối thiểu
    } else {
      // Format to 2 decimal places
      this.total = parseFloat(this.total.toFixed(2));
    }

    console.log('Final total for checkout:', this.total);

    // Set default orderForm values
    this.orderForm = {
      ...this.orderForm,
      paymentMethod: 'COD'
    };

    // Khởi tạo nút PayPal sau khi DOM đã sẵn sàng
    setTimeout(() => {
      this.renderPayPalButton();
    }, 1000);
  }

  showDepartmentClick() {
    this.showDepartment = !this.showDepartment;
  }

  onPaymentMethodChange(method: string, event: any) {
    const checked = event.target.checked;

    if (method === 'COD') {
      if (checked) {
        // Nếu người dùng tick vào COD
        this.orderForm.paymentMethod = 'COD';
      } else {
        // Nếu người dùng bỏ tick COD
        this.orderForm.paymentMethod = null;
      }
    } else if (method === 'BANK') {
      if (checked) {
        // Nếu người dùng tick vào BANK
        this.orderForm.paymentMethod = 'BANK';
      } else {
        // Nếu người dùng bỏ tick BANK
        this.orderForm.paymentMethod = null;
      }
    }
  }

  // Phương thức mới cho nút bấm chọn phương thức thanh toán
  selectPaymentMethod(method: string) {
    if (this.orderForm.paymentMethod === method) {
      // Nếu đã chọn rồi, bỏ chọn (toggle)
      this.orderForm.paymentMethod = null;
    } else {
      // Nếu chưa chọn, chọn mới
      this.orderForm.paymentMethod = method;

      // Show notification for bank transfer with more prominent warning
      if (method === 'BANK') {
        this.messageService.add({
          severity: 'warn',
          summary: 'Feature In Development',
          detail: 'This feature is currently under development. Please select another payment method.',
          sticky: true
        });
      }
    }
  }

  placeOrder() {
    // Validate form
    if (!this.validateForm()) {
      return;
    }

    // Loại bỏ toàn bộ logic liên quan đến VNPay payment

    // Check if bank transfer is selected and show message
    if (this.orderForm.paymentMethod === 'BANK') {
      this.messageService.add({
        severity: 'warn',
        summary: 'Feature In Development',
        detail: 'This feature is currently under development. Please select another payment method.',
        sticky: true
      });
      return;
    }

    // Get form values
    const { firstname, lastname, country, address, town, state, phone, email, note } = this.orderForm;

    // Kiểm tra giỏ hàng có sản phẩm không
    const cartItems = this.cartService.getItems();
    if (!cartItems || cartItems.length === 0) {
      console.error('Cart is empty when trying to create order');
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Your cart is empty. Please add products to your cart before checkout.'
      });
      return;
    }

    // Get order details from cart with correct format
    this.listOrderDetail = cartItems.map(item => {
      return {
        name: item.name,
        price: parseInt(String(item.price)), // Đảm bảo price là số nguyên
        quantity: item.quantity
      };
    });

    console.log('Order details before sending:', this.listOrderDetail);

    // Kiểm tra xem danh sách chi tiết đơn hàng có trống không
    if (!this.listOrderDetail || this.listOrderDetail.length === 0) {
      console.error('Order details list is empty');
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Order details list cannot be empty. Please refresh and try again.'
      });
      return;
    }

    // Place order using improved OrderService
    this.orderService.placeOrder(
      firstname,
      lastname,
      country,
      address,
      town,
      state,
      "", // Empty postCode
      phone,
      email,
      note,
      this.listOrderDetail,
      this.username
    ).subscribe({
      next: res => {
        this.placedOrder = res;
        this.orderSuccess = true;

        // Lưu đơn hàng mới vào localStorage
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
          status: 'Unpaid'
        };
        this.saveOrderToLocalStorage(order);

        // Clear cart only after successful order creation
        this.cartService.clearCart();

        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Order placed successfully!'
        });

        // Navigate to my orders page after successful order
        setTimeout(() => {
          this.router.navigate(['/my-order']);
        }, 2000);
      },
      error: err => {
        console.error('Error placing order:', err);
        // Hiển thị thông báo lỗi chi tiết
        let errorMessage = 'An error occurred while placing the order.';
        if (err.error && err.error.message) {
          errorMessage += ' Details: ' + err.error.message;
        }

        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: errorMessage
        });
      }
    });
  }

  // Thêm phương thức mới để lưu đơn hàng vào localStorage
  saveOrderToLocalStorage(order: any) {
    // Lấy danh sách đơn hàng hiện tại từ localStorage
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

    // Thêm đơn hàng mới vào danh sách
    // Tạo một ID tạm thời nếu chưa có
    if (!order.id) {
      order.id = Date.now(); // Sử dụng timestamp làm ID tạm thời
    }

    orders.push(order);

    // Lưu danh sách đơn hàng đã cập nhật vào localStorage
    localStorage.setItem(`orders_${this.username}`, JSON.stringify(orders));

    // Cũng lưu đơn hàng mới vào danh sách tất cả đơn hàng
    const allOrdersJson = localStorage.getItem('orders_all');
    let allOrders: any[] = [];

    if (allOrdersJson) {
      try {
        allOrders = JSON.parse(allOrdersJson);
      } catch (e) {
        console.error('Error parsing all orders from localStorage:', e);
        allOrders = [];
      }
    }

    allOrders.push(order);
    localStorage.setItem('orders_all', JSON.stringify(allOrders));

    // Lưu chi tiết đơn hàng riêng biệt
    localStorage.setItem(`order_${order.id}`, JSON.stringify(order));

    console.log('Order saved to localStorage:', order);
  }

  validateForm() {
    const requiredFields = ['firstname', 'lastname', 'country', 'address', 'town', 'state', 'email', 'phone'];

    for (const field of requiredFields) {
      if (!this.orderForm[field]) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: `Please fill in all required fields`
        });
        return false;
      }
    }

    // Kiểm tra định dạng số điện thoại
    const phoneRegex = /^\d{10,15}$/;
    if (!phoneRegex.test(this.orderForm.phone)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Phone number must be between 10 and 15 digits'
      });
      return false;
    }

    // Kiểm tra định dạng email @gmail.com
    const emailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    if (!emailRegex.test(this.orderForm.email)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Email must be in the correct format and end with @gmail.com'
      });
      return false;
    }

    return true;
  }

  renderPayPalButton() {
    // Đảm bảo tổng tiền là một số dương và hợp lệ
    if (!this.total || isNaN(this.total) || this.total <= 0) {
      console.error('Invalid total price:', this.total);
      this.total = 1; // Giá trị mặc định để tránh lỗi
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Invalid total price. Using default value.'
      });
    }

    const formattedTotal = parseFloat(this.total.toFixed(2));
    console.log('Formatted total for PayPal:', formattedTotal);

    paypal.Buttons({
      createOrder: (data: any, actions: any) => {
        console.log('Calling PayPal API with total:', formattedTotal);

        // Thêm logging để kiểm tra request
        const requestBody = {
          total: formattedTotal,
          description: 'Thanh toán đơn hàng Ogani'
        };
        console.log('Request to backend:', requestBody);

        return fetch('http://localhost:8080/api/paypal/pay', {
          method: 'post',
          headers: { 'content-type': 'application/json' },
          body: JSON.stringify(requestBody)
        })
          .then(async (res: any) => {
            console.log('Response status:', res.status);
            console.log('Response headers:', res.headers);

            // Đọc response body dưới dạng text trước
            const responseText = await res.text();
            console.log('Response raw text:', responseText);

            if (!res.ok) {
              throw new Error(responseText || `Network error: ${res.status}`);
            }

            // Kiểm tra nếu response rỗng
            if (!responseText || responseText.trim() === '') {
              console.error('Empty API response received');
              throw new Error('Empty response from server');
            }

            // Parse JSON từ text
            try {
              return JSON.parse(responseText);
            } catch (e) {
              console.error('Invalid JSON response:', e);
              console.error('Response was:', responseText);
              throw new Error('Invalid response format');
            }
          })
          .then((data: any) => {
            console.log('PayPal API parsed response:', data);

            // Kiểm tra dữ liệu response
            if (!data) {
              throw new Error('Empty data from backend');
            }

            // Kiểm tra orderId
            if (!data.orderId) {
              console.error('Response is missing orderId:', data);
              throw new Error('Không nhận được orderId từ backend');
            }

            return data.orderId;
          })
          .catch(err => {
            console.error('PayPal createOrder error:', err);
            this.messageService.add({
              severity: 'error',
              summary: 'PayPal Error',
              detail: 'Error creating PayPal order: ' + err.message
            });
            throw err;
          });
      },
      onApprove: (data: any, actions: any) => {
        // Validate form trước khi xử lý thanh toán
        if (!this.validateForm()) {
          return {
            error: 'Form validation failed'
          };
        }

        // Gọi API backend để capture order
        return fetch('http://localhost:8080/api/paypal/capture', {
          method: 'post',
          headers: {
            'content-type': 'application/json'
          },
          body: JSON.stringify({
            orderId: data.orderID
          })
        })
          .then((res: any) => {
            if (!res.ok) {
              throw new Error('PayPal capture failed');
            }
            return res.json();
          })
          .then((details: any) => {
            console.log('PayPal payment completed:', details);

            // Validate form trước khi tiếp tục
            if (!this.validateForm()) {
              return {
                error: 'Form validation failed'
              };
            }

            // Lấy thông tin form
            const { firstname, lastname, country, address, town, state, phone, email, note } = this.orderForm;

            // Kiểm tra giỏ hàng có sản phẩm không
            const cartItems = this.cartService.getItems();
            if (!cartItems || cartItems.length === 0) {
              console.error('Cart is empty when trying to create order with PayPal');
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Your cart is empty. Please add products to your cart before checkout.'
              });
              return {
                error: 'Empty cart'
              };
            }

            // Lấy chi tiết đơn hàng từ giỏ hàng với định dạng đúng
            this.listOrderDetail = cartItems.map(item => {
              return {
                name: item.name,
                price: parseInt(String(item.price)), // Đảm bảo price là số nguyên
                quantity: item.quantity
              };
            });

            console.log('Order details before sending:', this.listOrderDetail);

            // Kiểm tra xem danh sách chi tiết đơn hàng có trống không
            if (!this.listOrderDetail || this.listOrderDetail.length === 0) {
              console.error('Order details list is empty');
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Order details list cannot be empty. Please refresh and try again.'
              });
              return {
                error: 'Empty order details'
              };
            }

            // Gọi API tạo đơn hàng với OrderService đã cải tiến
            return new Promise((resolve, reject) => {
              this.orderService.placeOrder(
                firstname,
                lastname,
                country,
                address,
                town,
                state,
                "", // Empty postCode
                phone,
                email,
                note,
                this.listOrderDetail,
                this.username
              ).subscribe({
                next: res => {
                  this.placedOrder = res;
                  this.orderSuccess = true;

                  // Lưu đơn hàng vào localStorage với thêm thông tin PayPal
                  const orderWithExtra = {
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
                    paymentMethod: 'PAYPAL',
                    status: 'Paid',
                    paypalOrderId: data.orderID,
                    paypalTransactionId: details.id
                  };
                  this.saveOrderToLocalStorage(orderWithExtra);

                  // Clear cart only after successful order creation
                  this.cartService.clearCart();

                  this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: 'Payment successful and order placed!'
                  });

                  // Chuyển hướng đến trang đơn hàng
                  setTimeout(() => {
                    this.router.navigate(['/my-order']);
                  }, 2000);

                  resolve({
                    success: true,
                    orderId: res.id
                  });
                },
                error: err => {
                  console.error('Error creating order after PayPal payment:', err);
                  // Hiển thị thông báo lỗi chi tiết
                  let errorMessage = 'Payment was successful but there was an error creating your order.';
                  if (err.error && err.error.message) {
                    errorMessage += ' Details: ' + err.error.message;
                  }

                  this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: errorMessage
                  });

                  reject(err);
                }
              });
            });
          })
          .catch(err => {
            console.error('PayPal capture error:', err);
            this.messageService.add({
              severity: 'error',
              summary: 'Payment Error',
              detail: 'There was an error processing your payment. Please try again.'
            });
          });
      },
      onCancel: () => {
        console.log('PayPal payment cancelled');
        this.messageService.add({
          severity: 'info',
          summary: 'Payment Cancelled',
          detail: 'You have cancelled the PayPal payment'
        });
      },
      onError: (err: any) => {
        console.error('PayPal error:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'PayPal Error',
          detail: 'An error occurred with PayPal. Please try again.'
        });
      }
    }).render('#paypal-button-container');
  }

  // Gọi khi người dùng bấm "Tôi đã thanh toán"
  confirmedPaid() {
    if (this.orderPendingData && this.orderPendingData.orderId) {
      this.ngbModalRef.close();
      // Điều hướng về trang vnpay-return để xác thực giao dịch (có thể truyền kèm orderId query param)
      this.router.navigate(['/vnpay-return'], { queryParams: { orderId: this.orderPendingData.orderId } });
    }
  }
}
