import { Component, OnInit } from '@angular/core';
import { OrderService } from 'src/app/_service/order.service';
import { MessageService } from 'primeng/api';
import { DashboardService } from 'src/app/_service/dashboard.service';

@Component({
  selector: 'app-order',
  templateUrl: './order.component.html',
  styleUrls: ['./order.component.css'],
  providers: [MessageService]
})
export class OrderComponent implements OnInit {

  listOrder: any[] = [];
  selectedOrder: any = null;
  showOrderDetails: boolean = false;
  paymentStatuses: string[] = ['Unpaid', 'Paid'];
  selectedPaymentStatus: string = 'Unpaid';

  constructor(
    private orderService: OrderService,
    private messageService: MessageService,
    private dashboardService: DashboardService
  ) { }

  ngOnInit(): void {
    this.getListOrder();
  }

  getListOrder() {
    // Try to get all orders directly
    this.orderService.getListOrder().subscribe({
      next: res => {
        console.log('Raw response from server:', res);

        if (res && Array.isArray(res)) {
          // Get all orders from the server
          const serverOrders = [...res]; // Create a copy to avoid reference issues

          // Normalize backend orders to UI model
          const normalizedOrders = serverOrders.map((o: any) => {
            const idNum = parseInt(String(o.orderId));
            const qty = o.quantity ?? 1;
            const total = o.price ?? 0;
            const perItemPrice = qty > 0 ? Math.round(total / qty) : total;
            const orderDetails = o.productName
              ? [
                {
                  name: o.productName,
                  price: perItemPrice,
                  quantity: qty,
                  subTotal: perItemPrice * qty,
                },
              ]
              : [];
            return {
              id: idNum,
              firstname: o.firstname,
              lastname: o.lastname,
              address: o.address,
              town: o.town,
              country: o.country,
              state: o.state,
              postCode: o.postCode,
              email: o.email,
              phone: o.phone,
              note: o.note,
              createdDate: new Date(idNum * 1000),
              totalPrice: total,
              paymentMethod: o.status === 'PAID' ? 'BANK' : 'COD',
              status: o.status === 'PAID' ? 'Paid' : (o.status || 'Unpaid'),
              orderDetails,
            };
          });

          console.log('All orders from server:', serverOrders);

          // Get canceled orders from localStorage
          const canceledOrders = JSON.parse(localStorage.getItem('canceledOrders') || '[]');
          console.log('Canceled orders from localStorage:', canceledOrders);

          // Filter out canceled orders
          this.listOrder = normalizedOrders.filter(order => {
            return !canceledOrders.includes(order.id);
          });

          console.log('Orders after filtering canceled ones:', this.listOrder);

          // Apply saved payment statuses from localStorage
          this.applySavedPaymentStatuses();

          // Sort orders by date (newest first)
          this.listOrder.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());

          console.log('Final sorted orders:', this.listOrder);
        } else if (res && typeof res === 'object') {
          // If the response is an object but not an array, try to extract the array
          const ordersArray = Object.values(res).find(value => Array.isArray(value)) as any[];
          if (ordersArray && ordersArray.length > 0) {
            // Get all orders from the extracted array
            const serverOrders = [...ordersArray]; // Create a copy to avoid reference issues

            // Normalize backend orders to UI model
            const normalizedOrders = serverOrders.map((o: any) => {
              const idNum = parseInt(String(o.orderId));
              const qty = o.quantity ?? 1;
              const total = o.price ?? 0;
              const perItemPrice = qty > 0 ? Math.round(total / qty) : total;
              const orderDetails = o.productName
                ? [
                  {
                    name: o.productName,
                    price: perItemPrice,
                    quantity: qty,
                    subTotal: perItemPrice * qty,
                  },
                ]
                : [];
              return {
                id: idNum,
                firstname: o.firstname,
                lastname: o.lastname,
                address: o.address,
                town: o.town,
                country: o.country,
                state: o.state,
                postCode: o.postCode,
                email: o.email,
                phone: o.phone,
                note: o.note,
                createdDate: new Date(idNum * 1000),
                totalPrice: total,
                paymentMethod: o.status === 'PAID' ? 'BANK' : 'COD',
                status: o.status === 'PAID' ? 'Paid' : (o.status || 'Unpaid'),
                orderDetails,
              };
            });

            console.log('All orders from extracted array:', serverOrders);

            // Get canceled orders from localStorage
            const canceledOrders = JSON.parse(localStorage.getItem('canceledOrders') || '[]');
            console.log('Canceled orders from localStorage:', canceledOrders);

            // Filter out canceled orders
            this.listOrder = normalizedOrders.filter(order => !canceledOrders.includes(order.id));

            console.log('Orders after filtering canceled ones:', this.listOrder);

            // Apply saved payment statuses from localStorage
            this.applySavedPaymentStatuses();

            // Sort orders by date (newest first)
            this.listOrder.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());

            console.log('Final sorted orders:', this.listOrder);
          } else {
            console.error('Invalid response format:', res);
            // Try to get orders from localStorage as a fallback
            this.getOrdersFromLocalStorage();
          }
        } else {
          console.error('Invalid response format:', res);
          // Try to get orders from localStorage as a fallback
          this.getOrdersFromLocalStorage();
        }
      },
      error: err => {
        console.error('Error loading orders:', err);
        // Try to get orders from localStorage as a fallback
        this.getOrdersFromLocalStorage();
      }
    });
  }

  // New method to get orders from localStorage
  getOrdersFromLocalStorage() {
    console.log('Getting orders from localStorage as fallback');

    // Get all orders from localStorage
    const orders = this.orderService.getOrdersFromLocalStorage('all');

    if (orders && orders.length > 0) {
      // Get canceled orders from localStorage
      const canceledOrders = JSON.parse(localStorage.getItem('canceledOrders') || '[]');
      console.log('Canceled orders from localStorage:', canceledOrders);

      // Filter out canceled orders
      this.listOrder = orders.filter(order => {
        // Check if the order ID is in the canceledOrders array
        return !canceledOrders.includes(order.id);
      });

      console.log('Orders after filtering canceled ones:', this.listOrder);

      // Apply saved payment statuses from localStorage
      this.applySavedPaymentStatuses();

      // Sort orders by date (newest first)
      this.listOrder.sort((a, b) => {
        return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime();
      });

      console.log('Final sorted orders from localStorage:', this.listOrder);
    } else {
      console.log('No orders found in localStorage');
    }
  }

  // New method to apply saved payment statuses from localStorage
  applySavedPaymentStatuses() {
    // Loop through all orders and apply saved payment statuses
    this.listOrder.forEach(order => {
      const savedStatus = localStorage.getItem(`order_status_${order.id}`);
      if (savedStatus) {
        order.status = savedStatus;
      }
    });
  }

  viewOrderDetails(order: any) {
    this.selectedOrder = { ...order };
    // No longer forcing payment method to COD
    this.showOrderDetails = true;

    // Get payment status from localStorage if available
    const savedStatus = localStorage.getItem(`order_status_${order.id}`);
    if (savedStatus) {
      this.selectedPaymentStatus = savedStatus;
    } else {
      // Set the payment status based on the order's status
      if (order.status) {
        // Map old status to new status
        if (order.status === 'Paid') {
          this.selectedPaymentStatus = 'Paid';
        } else {
          this.selectedPaymentStatus = 'Unpaid';
        }
      } else {
        this.selectedPaymentStatus = 'Unpaid';
      }
    }
  }

  updatePaymentStatus(orderId: number, status: string) {
    const backendStatus = status === 'Paid' ? 'PAID' : 'UNPAID';
    const statusLabel = status === 'Paid' ? 'Đã thanh toán' : 'Chưa thanh toán';

    this.orderService.updateOrderStatus(String(orderId), backendStatus).subscribe({
      next: () => {
        const orderIndex = this.listOrder.findIndex(order => order.id === orderId);
        if (orderIndex !== -1) {
          this.listOrder[orderIndex].status = status;
        }

        localStorage.setItem(`order_status_${orderId}`, status);

        if (this.selectedOrder && this.selectedOrder.id === orderId) {
          this.selectedOrder.status = status;
        }

        this.updateOrdersInLocalStorage();

        this.dashboardService.notifyOrdersUpdated();

        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: `Đơn hàng đã được cập nhật thành ${statusLabel}`
        });
      },
      error: (error) => {
        console.error('Error updating payment status via backend:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể cập nhật trạng thái thanh toán. Vui lòng thử lại.'
        });
      }
    });
  }

  // Phương thức mới để cập nhật orders_all trong localStorage
  private updateOrdersInLocalStorage() {
    // Lưu danh sách đơn hàng hiện tại vào localStorage
    localStorage.setItem('orders_all', JSON.stringify(this.listOrder));
  }

  refreshOrders() {
    // Xóa cache trước khi tải lại
    localStorage.removeItem('orders_cache');
    localStorage.removeItem('orders_cache_timestamp');

    // Đảm bảo dữ liệu admin luôn mới nhất
    localStorage.removeItem('orders_all');

    // Tải lại danh sách đơn hàng
    this.getListOrder();

    // Thông báo cho DashboardService biết rằng đơn hàng đã được cập nhật
    this.dashboardService.notifyOrdersUpdated();

    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Đơn hàng đã được làm mới thành công'
    });
  }

  showSuccess(message: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: message
    });
  }

  showError(message: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: message
    });
  }

  showWarn(message: string) {
    this.messageService.add({
      severity: 'warn',
      summary: 'Warning',
      detail: message
    });
  }

  cancelOrder(orderId: number) {
    this.orderService.cancelOrder(orderId).subscribe({
      next: (response: any) => {
        const adminCanceledOrdersJson = localStorage.getItem('canceledOrders');
        let adminCanceledOrders: number[] = [];
        if (adminCanceledOrdersJson) {
          try {
            adminCanceledOrders = JSON.parse(adminCanceledOrdersJson);
          } catch (e) {
            console.error('Error parsing admin canceled orders:', e);
          }
        }

        if (!adminCanceledOrders.includes(orderId)) {
          adminCanceledOrders.push(orderId);
          localStorage.setItem('canceledOrders', JSON.stringify(adminCanceledOrders));
        }

        // Xóa khỏi danh sách hiện tại
        this.listOrder = this.listOrder.filter(order => order.id !== orderId);

        // Đóng dialog chi tiết nếu đang mở chính đơn này
        if (this.showOrderDetails && this.selectedOrder && this.selectedOrder.id === orderId) {
          this.showOrderDetails = false;
        }

        // Cập nhật cache admin
        localStorage.setItem('orders_all', JSON.stringify(this.listOrder));

        // Thông báo dashboard cập nhật
        this.dashboardService.notifyOrdersUpdated();

        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Đơn hàng đã được hủy và xóa thành công'
        });
      },
      error: (error: any) => {
        console.error('Error cancelling order:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to cancel order'
        });
      }
    });
  }
}
