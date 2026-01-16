import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from 'src/app/_service/order.service';
import { MessageService } from 'primeng/api';
import { Order } from 'src/app/_class/order';
import { StorageService } from 'src/app/_service/storage.service';
import { NgForm } from '@angular/forms';

@Component({
  selector: 'app-my-order',
  templateUrl: './my-order.component.html',
  styleUrls: ['./my-order.component.css'],
  providers: [MessageService],
})
export class MyOrderComponent implements OnInit {
  listOrder: Order[] = [];
  showOrderDetails: boolean = false;
  selectedOrder: Order | null = null;
  username: string = '';
  userId: number | null = null;

  // New properties for edit functionality
  showEditOrderDialog: boolean = false;
  editOrderForm: any = {};
  submitting: boolean = false;

  constructor(
    private orderService: OrderService,
    private route: ActivatedRoute,
    private router: Router,
    private messageService: MessageService,
    private storageService: StorageService
  ) { }

  ngOnInit(): void {
    // Sử dụng StorageService để lấy thông tin người dùng
    const user = this.storageService.getUser();
    if (user && user.username) {
      this.username = user.username;
      if (user.id !== undefined && user.id !== null) {
        this.userId = Number(user.id);
      } else if (user.userId !== undefined && user.userId !== null) {
        this.userId = Number(user.userId);
      } else {
        this.userId = null;
      }
      this.getListOrder();
    } else {
      // Nếu không có thông tin đăng nhập, chuyển hướng về trang đăng nhập
      this.router.navigate(['/login']);
    }
  }

  getListOrder() {
    // Clear cached data
    localStorage.removeItem('orders_cache');
    localStorage.removeItem('orders_cache_timestamp');

    // Get canceled orders from localStorage
    const canceledOrdersJson = localStorage.getItem('canceled_orders');
    let canceledOrders: number[] = [];
    if (canceledOrdersJson) {
      try {
        canceledOrders = JSON.parse(canceledOrdersJson);
      } catch (e) {
        console.error('Error parsing canceled orders:', e);
        canceledOrders = [];
      }
    }

    // Trước tiên, kiểm tra xem có đơn hàng mới trong localStorage không
    const userOrdersJson = localStorage.getItem(`orders_${this.username}`);
    if (userOrdersJson) {
      try {
        const userOrders = JSON.parse(userOrdersJson);
        if (userOrders && userOrders.length > 0) {
          // Lọc bỏ các đơn hàng đã hủy
          this.listOrder = userOrders.filter(
            (order: Order) => !canceledOrders.includes(order.id)
          );

          // Sắp xếp theo ngày (mới nhất lên đầu)
          this.listOrder.sort((a, b) => {
            return (
              new Date(b.createdDate).getTime() -
              new Date(a.createdDate).getTime()
            );
          });

          console.log('Using orders from localStorage:', this.listOrder);

          // Lưu vào cache để sử dụng sau này
          localStorage.setItem('orders_cache', JSON.stringify(this.listOrder));
          localStorage.setItem(
            'orders_cache_timestamp',
            new Date().toISOString()
          );

          // Vẫn gọi API để cập nhật dữ liệu mới nhất
          this.fetchOrdersFromServer(canceledOrders);
          return;
        }
      } catch (e) {
        console.error('Error parsing user orders from localStorage:', e);
      }
    }

    // Nếu không có đơn hàng trong localStorage, gọi API
    this.fetchOrdersFromServer(canceledOrders);
  }

  // Tách phương thức gọi API thành một phương thức riêng
  fetchOrdersFromServer(canceledOrders: number[]) {
    console.log('Fetching orders from server for userId:', this.userId);

    if (this.userId === null) {
      this.getOrdersFromLocalStorage();
      return;
    }

    this.orderService.getListOrderByUserId(this.userId).subscribe({
      next: (data: any) => {
        console.log('Server response:', data);
        if (data && Array.isArray(data)) {
          // Normalize backend orders to UI model
          const normalizedOrders: Order[] = data.map((o: any) => {
            const idNum = parseInt(String(o.orderId));
            const qty = o.quantity ?? 1;
            const total = o.price ?? 0;
            const perItemPrice = qty > 0 ? Math.round(total / qty) : total;

            let orderDetails = [];
            if (o.orderDetails && o.orderDetails.length > 0) {
              orderDetails = o.orderDetails;
            } else if (o.productName) {
              orderDetails = [
                {
                  name: o.productName,
                  price: perItemPrice,
                  quantity: qty,
                  subTotal: perItemPrice * qty,
                },
              ];
            }

            return {
              id: idNum,
              firstname: o.firstname,
              lastname: o.lastname,
              email: o.email,
              phone: o.phone,
              address: o.address,
              town: o.town,
              state: o.state,
              country: o.country,
              postCode: o.postCode,
              note: o.note,
              createdDate: new Date(idNum * 1000),
              totalPrice: total,
              paymentMethod: o.paymentMethod || localStorage.getItem(`order_payment_method_${idNum}`) || 'COD',
              status: o.status === 'PAID' ? 'Paid' : (o.status || 'Unpaid'),
              orderDetails,
            } as Order;
          });

          // Filter out canceled orders
          this.listOrder = normalizedOrders.filter(
            (order) => !canceledOrders.includes(order.id)
          );

          // Sort orders by date (newest first)
          this.listOrder.sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());

          // Save to localStorage for backup
          localStorage.setItem('orders_cache', JSON.stringify(this.listOrder));
          localStorage.setItem(
            'orders_cache_timestamp',
            new Date().toISOString()
          );

          // Cũng lưu vào danh sách đơn hàng của người dùng
          localStorage.setItem(
            `orders_${this.username}`,
            JSON.stringify(this.listOrder)
          );

          console.log('Filtered orders from server:', this.listOrder);
        } else {
          console.error('Invalid data format received from server');
          this.getOrdersFromLocalStorage();
        }
      },
      error: (error: any) => {
        console.error('Error fetching orders:', error);
        this.getOrdersFromLocalStorage();
      },
    });
  }

  getOrdersFromLocalStorage() {
    const cachedOrders = localStorage.getItem('orders_cache');
    const cacheTimestamp = localStorage.getItem('orders_cache_timestamp');

    if (cachedOrders && cacheTimestamp) {
      try {
        const orders = JSON.parse(cachedOrders);
        const cacheDate = new Date(cacheTimestamp);
        const now = new Date();
        const cacheAge = now.getTime() - cacheDate.getTime();

        // Use cache if it's less than 5 minutes old
        if (cacheAge < 5 * 60 * 1000) {
          // Get canceled orders
          const canceledOrdersJson = localStorage.getItem('canceled_orders');
          let canceledOrders: number[] = [];
          if (canceledOrdersJson) {
            try {
              canceledOrders = JSON.parse(canceledOrdersJson);
            } catch (e) {
              console.error('Error parsing canceled orders:', e);
              canceledOrders = [];
            }
          }

          // Filter out canceled orders
          this.listOrder = orders.filter(
            (order: Order) => !canceledOrders.includes(order.id)
          );

          // Sort orders by date (newest first)
          this.listOrder.sort((a, b) => {
            return (
              new Date(b.createdDate).getTime() -
              new Date(a.createdDate).getTime()
            );
          });

          console.log('Using cached orders:', this.listOrder);
          return;
        }
      } catch (e) {
        console.error('Error parsing cached orders:', e);
      }
    }

    // If we get here, either there's no cache or it's invalid/too old
    this.listOrder = [];
  }

  viewOrderDetails(order: Order) {
    this.selectedOrder = { ...order };
    // Không ghi đè phương thức thanh toán mà sử dụng giá trị từ đơn hàng
    this.showOrderDetails = true;
  }

  // New method to open the edit order dialog
  editOrder(order: Order) {
    this.selectedOrder = { ...order };
    this.editOrderForm = {
      firstname: order.firstname,
      lastname: order.lastname,
      email: order.email,
      phone: order.phone,
      address: order.address,
      town: order.town,
      state: order.state,
      country: order.country,
      note: order.note || '',
    };
    this.showEditOrderDialog = true;
  }

  // New method to save edited order
  saveOrderChanges() {
    if (!this.selectedOrder) return;

    this.submitting = true;

    // Update order with form data
    const updatedOrder: Order = {
      ...this.selectedOrder,
      firstname: this.editOrderForm.firstname,
      lastname: this.editOrderForm.lastname,
      email: this.editOrderForm.email,
      phone: this.editOrderForm.phone,
      address: this.editOrderForm.address,
      town: this.editOrderForm.town,
      state: this.editOrderForm.state,
      country: this.editOrderForm.country,
      note: this.editOrderForm.note,
      username: this.username, // Ensure username is included for storage
    };

    this.orderService.updateOrder(updatedOrder).subscribe({
      next: (response) => {
        this.submitting = false;
        if (response.success) {
          // Close dialog
          this.showEditOrderDialog = false;

          // Update the order in the list
          const index = this.listOrder.findIndex(
            (order) => order.id === updatedOrder.id
          );
          if (index !== -1) {
            this.listOrder[index] = updatedOrder;
          }

          // Show success message
          this.showSuccess('Order updated successfully');

          // If details dialog is open, update the selected order there too
          if (
            this.showOrderDetails &&
            this.selectedOrder &&
            this.selectedOrder.id === updatedOrder.id
          ) {
            this.selectedOrder = updatedOrder;
          }
        } else {
          this.showError(response.message || 'Could not update the order');
        }
      },
      error: (err) => {
        this.submitting = false;
        this.showError('An error occurred while updating the order');
        console.error('Error updating order:', err);
      },
    });
  }

  cancelOrder(orderId: number) {
    this.orderService.cancelOrder(orderId).subscribe({
      next: (response: any) => {
        // Add to canceled orders in localStorage - both for user and admin
        // Update the list of canceled orders for the user
        const canceledOrdersJson = localStorage.getItem('canceled_orders');
        let canceledOrders: number[] = [];
        if (canceledOrdersJson) {
          try {
            canceledOrders = JSON.parse(canceledOrdersJson);
          } catch (e) {
            console.error('Error parsing canceled orders:', e);
          }
        }

        if (!canceledOrders.includes(orderId)) {
          canceledOrders.push(orderId);
          localStorage.setItem(
            'canceled_orders',
            JSON.stringify(canceledOrders)
          );
        }

        // Update the list of canceled orders for admin
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
          localStorage.setItem(
            'canceledOrders',
            JSON.stringify(adminCanceledOrders)
          );
        }

        // Remove from all order storages
        this.removeOrderFromAllStorages(orderId);

        // Update the list
        this.listOrder = this.listOrder.filter((order) => order.id !== orderId);

        // Close order details dialog if open
        if (
          this.showOrderDetails &&
          this.selectedOrder &&
          this.selectedOrder.id === orderId
        ) {
          this.showOrderDetails = false;
        }

        this.showSuccess('Order cancelled successfully');
      },
      error: (error: any) => {
        console.error('Error cancelling order:', error);
        this.showError('Failed to cancel order');
      },
    });
  }

  // Thêm phương thức để xóa đơn hàng khỏi tất cả các localStorage
  private removeOrderFromAllStorages(orderId: number): void {
    // Cập nhật tất cả các storage có thể chứa đơn hàng
    const storageKeys = [
      'orders_cache',
      'orders_all', // admin cache
      `orders_${this.username}`, // user specific cache
    ];

    // Duyệt qua các key và cập nhật
    storageKeys.forEach((key) => {
      const ordersJson = localStorage.getItem(key);
      if (ordersJson) {
        try {
          const orders = JSON.parse(ordersJson);
          if (Array.isArray(orders)) {
            const updatedOrders = orders.filter(
              (order: any) => order.id !== orderId
            );
            localStorage.setItem(key, JSON.stringify(updatedOrders));
            console.log(`Updated ${key} after cancellation`);
          }
        } catch (e) {
          console.error(`Error updating ${key}:`, e);
        }
      }
    });
  }

  refreshOrders() {
    this.getListOrder();
    this.showSuccess('Đơn hàng đã được làm mới');
  }

  showSuccess(message: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: message,
    });
  }

  showError(message: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: message,
    });
  }

  showWarn(message: string) {
    this.messageService.add({
      severity: 'warn',
      summary: 'Warning',
      detail: message,
    });
  }
}
