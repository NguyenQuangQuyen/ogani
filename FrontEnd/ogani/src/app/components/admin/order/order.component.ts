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

          console.log('All orders from server:', serverOrders);

          // Get canceled orders from localStorage
          const canceledOrders = JSON.parse(localStorage.getItem('canceledOrders') || '[]');
          console.log('Canceled orders from localStorage:', canceledOrders);

          // Filter out canceled orders
          this.listOrder = serverOrders.filter(order => {
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

          console.log('Final sorted orders:', this.listOrder);
        } else if (res && typeof res === 'object') {
          // If the response is an object but not an array, try to extract the array
          const ordersArray = Object.values(res).find(value => Array.isArray(value)) as any[];
          if (ordersArray && ordersArray.length > 0) {
            // Get all orders from the extracted array
            const serverOrders = [...ordersArray]; // Create a copy to avoid reference issues

            console.log('All orders from extracted array:', serverOrders);

            // Get canceled orders from localStorage
            const canceledOrders = JSON.parse(localStorage.getItem('canceledOrders') || '[]');
            console.log('Canceled orders from localStorage:', canceledOrders);

            // Filter out canceled orders
            this.listOrder = serverOrders.filter(order => {
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
    // In a real application, you would call an API to update the payment status
    // For now, we'll just update the local data
    const orderIndex = this.listOrder.findIndex(order => order.id === orderId);
    if (orderIndex !== -1) {
      this.listOrder[orderIndex].status = status;

      // Save the payment status to localStorage
      localStorage.setItem(`order_status_${orderId}`, status);

      // Also update the selected order if it's the same one
      if (this.selectedOrder && this.selectedOrder.id === orderId) {
        this.selectedOrder.status = status;
      }

      // Cập nhật orders_all trong localStorage để đảm bảo dashboard lấy dữ liệu mới nhất
      this.updateOrdersInLocalStorage();

      // Thông báo cho DashboardService biết rằng đơn hàng đã được cập nhật
      this.dashboardService.notifyOrdersUpdated();

      // Show success message
      this.messageService.add({
        severity: 'success',
        summary: 'Success',
        detail: `Payment status updated to ${status}`
      });
    }
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
      detail: 'Orders refreshed successfully'
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
}
