import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { CartService } from 'src/app/_service/cart.service';
import { OrderService } from 'src/app/_service/order.service';

@Component({
  selector: 'app-payos-return',
  templateUrl: './payos-return.component.html',
  styleUrls: ['./payos-return.component.css'],
  providers: [MessageService],
})
export class PayosReturnComponent implements OnInit {
  loading = true;
  success = false;
  error = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private cartService: CartService,
    private orderService: OrderService
  ) { }

  ngOnInit(): void {
    // Kiểm tra query parameters từ PayOS
    this.route.queryParams.subscribe((params) => {
      console.log('PayOS return params:', params);

      // Lấy orderCode từ params (PayOS trả về trong returnUrl)
      const orderCode = params['orderCode'] || params['code'];
      const status = params['status'];

      // Kiểm tra status từ PayOS
      if (status === 'success' || params['code'] === '00') {
        // Thanh toán thành công
        this.success = true;
        this.successMessage = 'Payment successful! Your order has been placed.';

        // Nếu có orderCode, cập nhật status trong backend
        if (orderCode) {
          this.updateOrderStatusInBackend(orderCode, 'PAID');
        }

        // Clear cart sau khi thanh toán thành công
        this.cartService.clearCart();

        this.messageService.add({
          severity: 'success',
          summary: 'Payment Successful',
          detail: 'Your payment has been processed successfully!',
        });

        this.loading = false;

        // Chuyển hướng đến trang đơn hàng sau 2 giây
        setTimeout(() => {
          this.router.navigate(['/my-order']);
        }, 2000);
      } else if (status === 'cancel' || params['code'] === '24') {
        // Người dùng hủy thanh toán
        this.error = true;
        this.errorMessage = 'Payment was cancelled.';

        // Nếu có orderCode, cập nhật status trong backend
        if (orderCode) {
          this.updateOrderStatusInBackend(orderCode, 'CANCELLED');
        }

        this.messageService.add({
          severity: 'warn',
          summary: 'Payment Cancelled',
          detail: 'You cancelled the payment process.',
        });

        this.loading = false;

        // Chuyển hướng về trang checkout sau 2 giây
        setTimeout(() => {
          this.router.navigate(['/checkout']);
        }, 2000);
      } else {
        // Lỗi thanh toán hoặc chưa xác định
        // Thử check status từ backend nếu có orderCode
        if (orderCode) {
          this.checkOrderStatus(orderCode);
        } else {
          this.error = true;
          this.errorMessage =
            params['message'] ||
            'Payment status unknown. Please check your order.';

          this.messageService.add({
            severity: 'error',
            summary: 'Payment Status Unknown',
            detail: this.errorMessage,
          });

          this.loading = false;

          // Chuyển hướng về trang checkout sau 3 giây
          setTimeout(() => {
            this.router.navigate(['/checkout']);
          }, 3000);
        }
      }
    });
  }

  // Cập nhật status trong backend
  private updateOrderStatusInBackend(orderCode: string, status: string): void {
    this.orderService.updateOrderStatus(orderCode, status).subscribe({
      next: (response) => {
        console.log('Order status updated successfully:', response);
      },
      error: (error) => {
        console.error('Error updating order status:', error);
        // Không hiển thị lỗi cho user vì webhook sẽ xử lý
      },
    });
  }

  // Kiểm tra status từ backend (fallback nếu webhook chưa xử lý)
  private checkOrderStatus(orderCode: string): void {
    this.orderService.getOrderStatus(orderCode).subscribe({
      next: (status) => {
        console.log('Current order status:', status);
        if (status === 'PAID') {
          this.success = true;
          this.successMessage =
            'Payment successful! Your order has been placed.';
          this.cartService.clearCart();
          this.messageService.add({
            severity: 'success',
            summary: 'Payment Successful',
            detail: 'Your payment has been processed successfully!',
          });
          this.loading = false;
          setTimeout(() => {
            this.router.navigate(['/my-order']);
          }, 2000);
        } else {
          this.error = true;
          this.errorMessage = 'Payment status: ' + status;
          this.messageService.add({
            severity: 'info',
            summary: 'Payment Status',
            detail: 'Current status: ' + status,
          });
          this.loading = false;
          setTimeout(() => {
            this.router.navigate(['/my-order']);
          }, 3000);
        }
      },
      error: (error) => {
        console.error('Error checking order status:', error);
        this.error = true;
        this.errorMessage =
          'Unable to verify payment status. Please check your order later.';
        this.messageService.add({
          severity: 'warn',
          summary: 'Status Check Failed',
          detail: this.errorMessage,
        });
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/my-order']);
        }, 3000);
      },
    });
  }
}
