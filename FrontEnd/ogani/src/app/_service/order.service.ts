import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Order } from '../_class/order';
import { OrderDetail } from '../_class/order-detail';

const ORDER_API = "http://localhost:8080/api/order/";
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};


@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(private http: HttpClient) { }


  getListOrder(): Observable<any> {
    // Try to get all orders using the main endpoint
    console.log('Fetching all orders from main endpoint');
    return this.http.get(ORDER_API, httpOptions);
  }


  getListOrderByUser(username: string): Observable<any> {
    // If username is 'all', use getListOrder to get all orders
    if (username === 'all') {
      console.log('Fetching all orders');
      return this.getListOrder();
    }

    // Otherwise, get orders for a specific user
    console.log('Fetching orders for user:', username);

    // Try different endpoint formats
    return this.http.get(ORDER_API + 'user/' + username, httpOptions);
  }

  // New method to get orders from localStorage as a fallback
  getOrdersFromLocalStorage(username: string): any[] {
    console.log('Getting orders from localStorage for user:', username);

    // Nếu là admin (username = 'all'), ưu tiên lấy dữ liệu từ 'orders_all'
    if (username === 'all') {
      const adminOrdersJson = localStorage.getItem('orders_all');
      if (adminOrdersJson) {
        try {
          const adminOrders = JSON.parse(adminOrdersJson);
          console.log('Found admin orders in localStorage:', adminOrders);
          return adminOrders;
        } catch (e) {
          console.error('Error parsing admin orders:', e);
        }
      }
    }

    // Get orders from localStorage for specific user
    const ordersJson = localStorage.getItem(`orders_${username}`);
    if (ordersJson) {
      try {
        const orders = JSON.parse(ordersJson);
        console.log('Found orders in localStorage for user:', orders);

        // Nếu là admin, cập nhật lại cache admin
        if (username === 'all') {
          localStorage.setItem('orders_all', JSON.stringify(orders));
        }

        return orders;
      } catch (e) {
        console.error('Error parsing orders from localStorage:', e);
        return [];
      }
    }

    // If no orders found for this user, try to get all orders
    if (username === 'all') {
      const allOrders: any[] = [];
      const processedIds = new Set(); // Tránh trùng lặp đơn hàng

      // Loop through all localStorage keys
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('orders_') && !key.includes('details')) {
          try {
            const orderJson = localStorage.getItem(key);
            if (orderJson) {
              // Check if the value is a valid JSON string
              if (this.isValidJson(orderJson)) {
                const orders = JSON.parse(orderJson);
                if (Array.isArray(orders)) {
                  orders.forEach(order => {
                    if (!processedIds.has(order.id)) {
                      allOrders.push(order);
                      processedIds.add(order.id);
                    }
                  });
                }
              } else {
                console.warn(`Skipping invalid JSON data for key: ${key}`);
              }
            }
          } catch (e) {
            console.error('Error parsing order from localStorage:', e);
          }
        }
      }

      console.log('Found all orders in localStorage:', allOrders);

      // Cập nhật cache cho admin
      if (allOrders.length > 0) {
        localStorage.setItem('orders_all', JSON.stringify(allOrders));
      }

      return allOrders;
    }

    return [];
  }

  // Helper method to check if a string is valid JSON
  private isValidJson(str: string): boolean {
    try {
      JSON.parse(str);
      return true;
    } catch (e) {
      return false;
    }
  }

  placeOrder(firstname: string, lastname: string, country: string, address: string, town: string, state: string, postCode: string, phone: string, email: string, note: string, orderDetails: OrderDetail[], username: string): Observable<any> {
    // Validate phone number (10-15 digits)
    const formattedPhone = String(phone || '').replace(/\D/g, '');
    if (formattedPhone.length < 10 || formattedPhone.length > 15) {
      return throwError(() => new Error('Phone number must be between 10 and 15 digits'));
    }

    // Validate email format
    const emailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    if (!emailRegex.test(email)) {
      return throwError(() => new Error('Email must be a valid Gmail address'));
    }

    // Tạo đối tượng request theo định dạng backend yêu cầu
    const orderRequest = {
      firstname: firstname.trim(),
      lastname: lastname.trim(),
      country: country.trim(),
      address: address.trim(),
      town: town.trim(),
      state: state.trim(),
      postCode: "", // Default empty string for postCode
      phone: formattedPhone,
      email: email.trim(),
      note: note ? note.trim() : '',
      // Chuyển đổi orderDetails để đảm bảo định dạng đúng
      orderDetails: orderDetails.map(item => ({
        name: item.name,
        price: typeof item.price === 'number' ? item.price : parseInt(String(item.price)),
        quantity: item.quantity
      })),
      username: username.trim()
    };

    console.log('Sending order request to backend:', orderRequest);
    return this.http.post(ORDER_API + 'create', orderRequest, httpOptions)
      .pipe(
        catchError(error => {
          console.error('Error creating order:', error);
          if (error.error && error.error.message) {
            return throwError(() => new Error(error.error.message));
          }
          return throwError(() => new Error('Failed to create order. Please try again.'));
        })
      );
  }

  // Thêm phương thức cancelOrder
  cancelOrder(orderId: number): Observable<any> {
    console.log('Cancelling order:', orderId);
    // Trong thực tế, bạn sẽ gọi API để hủy đơn hàng
    // Nhưng vì chúng ta đang xử lý trực tiếp trong component, nên chỉ trả về một Observable rỗng
    return of({ success: true });
  }

  // Thêm phương thức updateOrder
  updateOrder(order: Order): Observable<any> {
    console.log('Updating order:', order);

    // Gửi request đến backend để cập nhật đơn hàng
    // Trong trường hợp thực tế, sẽ có API endpoint để cập nhật đơn hàng
    // Ví dụ: return this.http.put(ORDER_API + 'update/' + order.id, order, httpOptions);

    // Hiện tại, chúng ta sẽ cập nhật trực tiếp vào localStorage và trả về kết quả thành công
    try {
      // Cập nhật order trong localStorage của user
      const ordersJson = localStorage.getItem(`orders_${order.username || 'all'}`);
      if (ordersJson) {
        const orders = JSON.parse(ordersJson);
        const index = orders.findIndex((o: Order) => o.id === order.id);
        if (index !== -1) {
          orders[index] = order;
          localStorage.setItem(`orders_${order.username || 'all'}`, JSON.stringify(orders));
        }
      }

      // Cập nhật trong cache chung
      const cacheJson = localStorage.getItem('orders_cache');
      if (cacheJson) {
        const cache = JSON.parse(cacheJson);
        const cacheIndex = cache.findIndex((o: Order) => o.id === order.id);
        if (cacheIndex !== -1) {
          cache[cacheIndex] = order;
          localStorage.setItem('orders_cache', JSON.stringify(cache));
        }
      }

      // Cập nhật trong danh sách đơn hàng của admin
      const adminOrdersJson = localStorage.getItem('orders_all');
      if (adminOrdersJson) {
        const adminOrders = JSON.parse(adminOrdersJson);
        const adminIndex = adminOrders.findIndex((o: Order) => o.id === order.id);
        if (adminIndex !== -1) {
          adminOrders[adminIndex] = order;
          localStorage.setItem('orders_all', JSON.stringify(adminOrders));
        }
      }

      // Đồng bộ thay đổi với tất cả các bộ nhớ cache có thể
      this.syncOrderAcrossAllStorage(order);

      return of({ success: true, message: 'Order updated successfully', data: order });
    } catch (e) {
      console.error('Error updating order in localStorage:', e);
      return of({ success: false, message: 'Failed to update order' });
    }
  }

  // Phương thức mới để đồng bộ hóa đơn hàng trong tất cả các lưu trữ
  private syncOrderAcrossAllStorage(order: Order): void {
    // Quét tất cả các key trong localStorage để tìm và cập nhật đơn hàng
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && (key.startsWith('orders_') || key.includes('order'))) {
        try {
          const dataJson = localStorage.getItem(key);
          if (dataJson) {
            const data = JSON.parse(dataJson);

            // Nếu là mảng, tìm và cập nhật đơn hàng
            if (Array.isArray(data)) {
              const index = data.findIndex((o: any) => o.id === order.id);
              if (index !== -1) {
                data[index] = order;
                localStorage.setItem(key, JSON.stringify(data));
                console.log(`Updated order in storage: ${key}`);
              }
            }
            // Nếu là đối tượng đơn lẻ và có cùng ID
            else if (data && typeof data === 'object' && 'id' in data && data.id === order.id) {
              localStorage.setItem(key, JSON.stringify(order));
              console.log(`Updated single order in storage: ${key}`);
            }
          }
        } catch (e) {
          console.error(`Error updating order in ${key}:`, e);
        }
      }
    }
  }
}
