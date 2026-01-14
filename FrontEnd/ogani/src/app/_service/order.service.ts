import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Order } from '../_class/order';
import { OrderDetail } from '../_class/order-detail';

const ORDER_API = 'http://hgr0a62zxby.sn.mynetname.net:2003/api/order/';
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  constructor(private http: HttpClient) { }

  getListOrder(): Observable<any> {
    return this.http.get(ORDER_API + 'getall', httpOptions);
  }

  getListOrderByUserId(userId: number): Observable<any> {
    return this.http.get(ORDER_API + 'user-id/' + userId, httpOptions);
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
                  orders.forEach((order) => {
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

  placeOrder(
    firstname: string,
    lastname: string,
    country: string,
    address: string,
    town: string,
    state: string,
    postCode: string,
    phone: string,
    email: string,
    note: string,
    orderDetails: OrderDetail[],
    username: string,
    userId: string
  ): Observable<any> {
    // Validate phone number (10-15 digits)
    const formattedPhone = String(phone || '').replace(/\D/g, '');
    if (formattedPhone.length < 10 || formattedPhone.length > 15) {
      return throwError(
        () => new Error('Phone number must be between 10 and 15 digits')
      );
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
      postCode: '', // Default empty string for postCode
      phone: formattedPhone,
      email: email.trim(),
      note: note ? note.trim() : '',
      // Chuyển đổi orderDetails để đảm bảo định dạng đúng
      orderDetails: orderDetails.map((item) => ({
        name: item.name,
        price:
          typeof item.price === 'number'
            ? item.price
            : parseInt(String(item.price)),
        quantity: item.quantity,
        productId: item.productId, // Thêm productId để track tồn kho
      })),
      username: username.trim(),
      userId: userId ? parseInt(userId) : null,
    };

    console.log('Sending COD order request to backend:', orderRequest);
    return this.http
      .post(ORDER_API + 'create_cod', orderRequest, httpOptions)
      .pipe(
        catchError((error) => {
          console.error('Error creating COD order:', error);
          if (error.error && error.error.message) {
            return throwError(() => new Error(error.error.message));
          }
          return throwError(
            () => new Error('Failed to create COD order. Please try again.')
          );
        })
      );
  }

  // Thêm phương thức cancelOrder
  cancelOrder(orderId: number): Observable<any> {
    console.log('Cancelling order via backend:', orderId);
    return this.http
      .delete(ORDER_API + 'delete/' + String(orderId), httpOptions)
      .pipe(
        map((data) => ({ success: true, data })),
        catchError((error) => {
          console.error('Error deleting order via backend:', error);
          return of({ success: false, message: 'Failed to cancel order' });
        })
      );
  }

  // Thêm phương thức updateOrder
  updateOrder(order: Order): Observable<any> {
    console.log('Updating order via backend:', order);

    const requestBody = {
      firstname: order.firstname,
      lastname: order.lastname,
      country: order.country,
      address: order.address,
      town: order.town,
      state: order.state,
      postCode: String(order.postCode ?? ''),
      email: order.email,
      phone: String(order.phone ?? ''),
      note: order.note ?? '',
    };

    const orderCode = String(order.id);
    return this.http
      .put(ORDER_API + 'update/' + orderCode, requestBody, httpOptions)
      .pipe(
        map((data) => ({ success: true, data })),
        catchError((error) => {
          console.error('Error updating order via backend:', error);
          return of({ success: false, message: 'Failed to update order' });
        })
      );
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
            else if (
              data &&
              typeof data === 'object' &&
              'id' in data &&
              data.id === order.id
            ) {
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

  // Create order with PayOS payment
  createOrderWithPayOS(
    userId: string,
    username: string,
    firstname: string,
    lastname: string,
    country: string,
    address: string,
    state: string,
    phone: string,
    note: string,
    town: string,
    postCode: string,
    email: string,
    productName: string,
    description: string,
    returnUrl: string,
    cancelUrl: string,
    price: number,
    productId: number | null,
    orderDetails: OrderDetail[] = []
  ): Observable<any> {
    const requestBody = {
      userId: userId ? parseInt(userId) : null,
      username: username,
      firstname: firstname,
      lastname: lastname,
      country: country,
      state: state,
      address: address,
      phone: phone,
      email: email,
      town: town,
      postCode: postCode || '',
      note: note || '',
      productName: productName,
      productId: productId,
      description: description,
      returnUrl: returnUrl,
      cancelUrl: cancelUrl,
      price: Math.round(price), // PayOS requires integer price
      orderDetails: orderDetails.map((item) => ({
        name: item.name,
        price:
          typeof item.price === 'number'
            ? item.price
            : parseInt(String(item.price)),
        quantity: item.quantity,
        productId: item.productId, // Thêm productId để track tồn kho
      })),
    };

    console.log('Creating PayOS order with request:', requestBody);
    return this.http.post(ORDER_API + 'create', requestBody, httpOptions).pipe(
      catchError((error) => {
        console.error('Error creating PayOS order:', error);
        if (error.error && error.error.message) {
          return throwError(() => new Error(error.error.message));
        }
        return throwError(
          () => new Error('Failed to create PayOS order. Please try again.')
        );
      })
    );
  }

  updateOrderStatus(orderCode: string, status: string): Observable<any> {
    const requestBody = { status: status };
    console.log('Updating order status:', orderCode, status);
    return this.http
      .put(ORDER_API + 'update_status/' + orderCode, requestBody, httpOptions)
      .pipe(
        catchError((error) => {
          console.error('Error updating order status:', error);
          if (error.error && error.error.message) {
            return throwError(() => new Error(error.error.message));
          }
          return throwError(
            () => new Error('Failed to update order status. Please try again.')
          );
        })
      );
  }

  // Get order status
  getOrderStatus(orderCode: string): Observable<string> {
    console.log('Getting order status for:', orderCode);
    return this.http
      .get<string>(ORDER_API + 'get_status/' + orderCode, httpOptions)
      .pipe(
        catchError((error) => {
          console.error('Error getting order status:', error);
          if (error.error && error.error.message) {
            return throwError(() => new Error(error.error.message));
          }
          return throwError(
            () => new Error('Failed to get order status. Please try again.')
          );
        })
      );
  }
}
