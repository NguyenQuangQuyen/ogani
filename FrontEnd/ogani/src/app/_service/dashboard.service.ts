import { Injectable, EventEmitter } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { OrderService } from './order.service';

export interface DashboardStatistics {
  totalSoldProducts: number;
  totalRevenue: number;
  bestSellingProduct: {
    name: string;
    quantity: number;
  };
  monthlySales: {
    month: string;
    revenue: number;
  }[];
  productSalesDistribution: {
    name: string;
    quantity: number;
    percentage: number;
  }[];
  recentOrders: any[];
  orderStatusCounts: {
    total: number;
    paid: number;
    unpaid: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  // Event emitter để thông báo khi có thay đổi dữ liệu đơn hàng
  public ordersChanged = new EventEmitter<void>();
  
  private cachedStatistics: DashboardStatistics | null = null;
  private lastFetchTime: number = 0;
  private cacheExpiryTime: number = 5 * 60 * 1000; // 5 phút

  constructor(
    private http: HttpClient,
    private orderService: OrderService
  ) { }

  /**
   * Get dashboard statistics from orders
   */
  getDashboardStatistics(): Observable<DashboardStatistics> {
    console.log('Fetching fresh data for dashboard');
    
    // Luôn ưu tiên lấy dữ liệu từ localStorage trước vì API đang gặp lỗi 500
    const localStorageData = this.getOrdersFromLocalStorage();
    
    if (localStorageData && localStorageData.length > 0) {
      console.log('Found orders in localStorage:', localStorageData.length);
      const statistics = this.processOrdersData(localStorageData);
      this.cachedStatistics = statistics;
      this.lastFetchTime = new Date().getTime();
      
      // Lưu thống kê vào localStorage để sử dụng offline
      localStorage.setItem('dashboard_statistics', JSON.stringify(statistics));
      
      return of(statistics);
    }
    
    // Nếu không có dữ liệu trong localStorage, thử gọi API
    return this.orderService.getListOrder().pipe(
      map(orders => {
        console.log('API data received from OrderService:', orders);
        const statistics = this.processOrdersData(orders);
        this.cachedStatistics = statistics;
        this.lastFetchTime = new Date().getTime();
        
        // Lưu thống kê vào localStorage để sử dụng offline
        localStorage.setItem('dashboard_statistics', JSON.stringify(statistics));
        
        return statistics;
      }),
      catchError(error => {
        console.error('Error fetching orders from API:', error);
        
        // Nếu không có dữ liệu trong localStorage và API lỗi, thử lấy từ cache
        console.log('No orders found in localStorage, checking dashboard cache');
        const cachedStatsJson = localStorage.getItem('dashboard_statistics');
        if (cachedStatsJson) {
          try {
            const cachedStats = JSON.parse(cachedStatsJson);
            console.log('Using cached dashboard statistics');
            return of(cachedStats);
          } catch (e) {
            console.error('Error parsing cached dashboard statistics:', e);
          }
        }
        
        // Nếu không có cache, trả về thống kê trống
        console.log('No cached statistics found, returning empty statistics');
        return of(this.getEmptyStatistics());
      })
    );
  }

  /**
   * Notify that orders have been updated
   */
  notifyOrdersUpdated(): void {
    console.log('Notifying orders updated');
    // Xóa cache để đảm bảo lấy dữ liệu mới nhất
    this.cachedStatistics = null;
    this.lastFetchTime = 0;
    localStorage.removeItem('dashboard_statistics');
    
    // Emit event để các component khác biết và cập nhật
    this.ordersChanged.emit();
  }

  /**
   * Get orders from localStorage with proper parsing
   */
  private getOrdersFromLocalStorage(): any[] {
    try {
      // Lấy dữ liệu từ localStorage
      const allOrdersJson = localStorage.getItem('orders_all');
      if (allOrdersJson) {
        const orders = JSON.parse(allOrdersJson);
        console.log('Raw orders from localStorage:', orders);
        return orders;
      }
      
      // Nếu không có 'orders_all', thử lấy từ OrderService
      return this.orderService.getOrdersFromLocalStorage('all');
    } catch (error) {
      console.error('Error getting orders from localStorage:', error);
      return [];
    }
  }

  /**
   * Process order data into dashboard statistics
   */
  private processOrdersData(orders: any[]): DashboardStatistics {
    if (!orders || !Array.isArray(orders) || orders.length === 0) {
      console.log('No orders data to process');
      return this.getEmptyStatistics();
    }

    console.log('Processing orders data:', orders.length, 'orders');

    // Lọc các đơn hàng đã thanh toán (chỉ lấy đơn hàng có trạng thái Paid)
    const paidOrders = orders.filter(order => {
      const status = String(order.status || '').toUpperCase();
      return status === 'PAID' || status === 'COMPLETED' || status === 'DELIVERED';
    });

    console.log('Paid orders:', paidOrders.length, paidOrders);

    // Tính tổng doanh thu - chỉ từ đơn hàng đã thanh toán
    let totalRevenue = 0;
    paidOrders.forEach(order => {
      let orderTotal = 0;
      
      // Thử chuyển đổi totalPrice từ nhiều định dạng khác nhau
      if (typeof order.totalPrice === 'number') {
        orderTotal = order.totalPrice;
      } else if (typeof order.totalPrice === 'string') {
        // Loại bỏ tất cả ký tự không phải số và dấu chấm thập phân
        const cleanedPrice = order.totalPrice.replace(/[^\d.]/g, '');
        orderTotal = parseFloat(cleanedPrice) || 0;
      }
      
      console.log(`Order ${order.id} total: ${orderTotal} (original: ${order.totalPrice})`);
      totalRevenue += orderTotal;
    });

    console.log('Total revenue calculated:', totalRevenue);

    // Tính số lượng sản phẩm và tìm sản phẩm bán chạy nhất - từ tất cả đơn hàng
    // Không lọc theo trạng thái, lấy tất cả đơn hàng để tính sản phẩm bán chạy
    const productMap = new Map<string, number>();
    let totalProducts = 0;

    // Xử lý tất cả đơn hàng để tìm sản phẩm bán chạy
    orders.forEach(order => {
      if (order.orderDetails && Array.isArray(order.orderDetails)) {
        order.orderDetails.forEach((detail: any) => {
          let quantity = 0;
          
          // Thử chuyển đổi quantity từ nhiều định dạng khác nhau
          if (typeof detail.quantity === 'number') {
            quantity = detail.quantity;
          } else if (typeof detail.quantity === 'string') {
            quantity = parseInt(detail.quantity) || 0;
          }
          
          totalProducts += quantity;
          
          const productName = detail.name || 'Unknown Product';
          const currentQuantity = productMap.get(productName) || 0;
          productMap.set(productName, currentQuantity + quantity);
        });
      }
    });

    console.log('Total products in all orders:', totalProducts);
    console.log('Product distribution:', Array.from(productMap.entries()));

    // Tìm sản phẩm bán chạy nhất
    let bestSellingProduct = { name: '', quantity: 0 };
    productMap.forEach((quantity, name) => {
      if (quantity > bestSellingProduct.quantity) {
        bestSellingProduct = { name, quantity };
      }
    });

    // Tính doanh thu theo tháng - chỉ từ đơn hàng đã thanh toán
    const monthlySales = this.calculateMonthlySales(paidOrders);

    // Tính phân phối sản phẩm - top 4 sản phẩm bán chạy
    const productSalesDistribution = this.calculateProductDistribution(productMap, totalProducts);

    // Lấy 5 đơn hàng gần nhất
    const recentOrders = this.getRecentOrders(orders);

    // Tính số lượng đơn hàng theo trạng thái
    const orderStatusCounts = this.calculateOrderStatusCounts(orders);

    return {
      totalSoldProducts: totalProducts,
      totalRevenue,
      bestSellingProduct,
      monthlySales,
      productSalesDistribution,
      recentOrders,
      orderStatusCounts
    };
  }

  /**
   * Calculate monthly sales data
   */
  private calculateMonthlySales(orders: any[]): { month: string, revenue: number }[] {
    const monthlyData = new Map<string, number>();
    
    // Khởi tạo tất cả các tháng
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    months.forEach(month => monthlyData.set(month, 0));
    
    // Tính doanh thu theo tháng
    orders.forEach(order => {
      if (order.createdDate) {
        const date = new Date(order.createdDate);
        const month = months[date.getMonth()];
        const revenue = monthlyData.get(month) || 0;
        
        // Xử lý totalPrice tương tự như trong processOrdersData
        let orderTotal = 0;
        if (typeof order.totalPrice === 'number') {
          orderTotal = order.totalPrice;
        } else if (typeof order.totalPrice === 'string') {
          const cleanedPrice = order.totalPrice.replace(/[^\d.]/g, '');
          orderTotal = parseFloat(cleanedPrice) || 0;
        }
        
        monthlyData.set(month, revenue + orderTotal);
      }
    });

    // Chuyển map thành mảng
    return months.map(month => ({
      month,
      revenue: monthlyData.get(month) || 0
    }));
  }

  /**
   * Calculate product distribution data
   */
  private calculateProductDistribution(productMap: Map<string, number>, totalProducts: number): any[] {
    if (totalProducts === 0) return [];
    
    const distribution = Array.from(productMap.entries())
      .map(([name, quantity]) => ({
        name,
        quantity,
        percentage: Math.round((quantity / totalProducts) * 100) // Làm tròn phần trăm
      }))
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 4); // Lấy top 4 sản phẩm
    
    return distribution;
  }

  /**
   * Get recent orders
   */
  private getRecentOrders(orders: any[]): any[] {
    return [...orders]
      .sort((a, b) => {
        const dateA = new Date(a.createdDate).getTime();
        const dateB = new Date(b.createdDate).getTime();
        return dateB - dateA; // Sắp xếp giảm dần (mới nhất trước)
      })
      .slice(0, 5); // Lấy 5 đơn hàng gần nhất
  }

  /**
   * Calculate order status counts
   */
  private calculateOrderStatusCounts(orders: any[]): any {
    const total = orders.length;
    const paid = orders.filter(order => {
      const status = String(order.status || '').toUpperCase();
      return status === 'PAID' || status === 'COMPLETED' || status === 'DELIVERED';
    }).length;
    
    const unpaid = orders.filter(order => {
      const status = String(order.status || '').toUpperCase();
      return status === 'UNPAID' || status === 'PENDING';
    }).length;

    return {
      total,
      paid,
      unpaid
    };
  }

  /**
   * Get empty statistics object
   */
  private getEmptyStatistics(): DashboardStatistics {
    return {
      totalSoldProducts: 0,
      totalRevenue: 0,
      bestSellingProduct: { name: '', quantity: 0 },
      monthlySales: [],
      productSalesDistribution: [],
      recentOrders: [],
      orderStatusCounts: {
        total: 0,
        paid: 0,
        unpaid: 0
      }
    };
  }

  /**
   * Force refresh dashboard data
   */
  forceRefresh(): Observable<DashboardStatistics> {
    // Xóa cache
    this.cachedStatistics = null;
    this.lastFetchTime = 0;
    
    // Xóa cache trong localStorage để đảm bảo lấy dữ liệu mới nhất
    localStorage.removeItem('dashboard_statistics');
    
    // Lấy dữ liệu mới
    return this.getDashboardStatistics();
  }
} 