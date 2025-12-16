import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { IconDefinition } from '@fortawesome/free-solid-svg-icons';
import {
  faFaceLaughWink,
  faTag,
  faSearch,
  faBell,
  faEnvelope,
  faTachometerAlt,
  faBookmark,
  faReceipt,
  faCartShopping,
  faRocket,
  faUser,
  faBars,
  faPaperPlane,
  faGear,
  faRightFromBracket,
  faUsers,
  faHome,
  faDownload,
  faShoppingBag,
  faDollarSign,
  faTrophy,
  faChartLine,
  faSync,
  faShoppingCart
} from '@fortawesome/free-solid-svg-icons';
import { AuthService } from 'src/app/_service/auth.service';
import { StorageService } from 'src/app/_service/storage.service';
import { DashboardService, DashboardStatistics } from 'src/app/_service/dashboard.service';
import { finalize } from 'rxjs/operators';
import { OrderService } from 'src/app/_service/order.service';
import { Subscription } from 'rxjs';
import { ReportApiService } from 'src/app/_service/report-api.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Định nghĩa icon đúng kiểu `IconDefinition`
  faceLaugh: IconDefinition = faFaceLaughWink;
  search: IconDefinition = faSearch;
  bell: IconDefinition = faBell;
  envelope: IconDefinition = faEnvelope;
  tachometer: IconDefinition = faTachometerAlt;
  bookmark: IconDefinition = faBookmark;
  receipt: IconDefinition = faReceipt;
  cart: IconDefinition = faCartShopping;
  rocket: IconDefinition = faRocket;
  userIcon: IconDefinition = faUser;
  paperPlane: IconDefinition = faPaperPlane;
  bars: IconDefinition = faBars;
  gear: IconDefinition = faGear;
  logoutIcon: IconDefinition = faRightFromBracket;
  tag: IconDefinition = faTag;
  users: IconDefinition = faUsers;
  home: IconDefinition = faHome;
  download: IconDefinition = faDownload;
  shoppingBag: IconDefinition = faShoppingBag;
  dollar: IconDefinition = faDollarSign;
  trophy: IconDefinition = faTrophy;
  chartLine: IconDefinition = faChartLine;
  refresh: IconDefinition = faSync;
  shoppingCart: IconDefinition = faShoppingCart;

  // Dashboard statistics
  statistics: DashboardStatistics | null = null;
  isLoading: boolean = true;
  error: string | null = null;

  // Subscription để theo dõi sự kiện thay đổi đơn hàng
  private ordersChangedSubscription: Subscription | null = null;

  constructor(
    private storageService: StorageService,
    private authService: AuthService,
    private router: Router,
    private dashboardService: DashboardService,
    private orderService: OrderService,
    private reportApi: ReportApiService
  ) { }

  ngOnInit(): void {
    // Đăng ký lắng nghe sự kiện thay đổi đơn hàng
    this.ordersChangedSubscription = this.dashboardService.ordersChanged.subscribe(() => {
      console.log('Orders changed event received, refreshing dashboard data');
      this.loadDashboardData();
    });

    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    // Hủy đăng ký lắng nghe sự kiện khi component bị hủy
    if (this.ordersChangedSubscription) {
      this.ordersChangedSubscription.unsubscribe();
      this.ordersChangedSubscription = null;
    }
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.error = null;

    // Lấy dữ liệu từ DashboardService
    this.dashboardService.getDashboardStatistics()
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (data) => {
          console.log('Dashboard data loaded:', data);
          this.statistics = data;
        },
        error: (err) => {
          console.error('Failed to load dashboard data:', err);
          this.error = 'Không thể tải dữ liệu tổng quan. Vui lòng thử lại sau.';
        }
      });
  }
  // Tính phần trăm đơn hàng đã thanh toán
  getOrderPaidPercentage(): number {
    if (!this.statistics || !this.statistics.orderStatusCounts || this.statistics.orderStatusCounts.total === 0) {
      return 0;
    }
    return Math.round((this.statistics.orderStatusCounts.paid / this.statistics.orderStatusCounts.total) * 100);
  }

  // Tính phần trăm đơn hàng chưa thanh toán
  getOrderUnpaidPercentage(): number {
    if (!this.statistics || !this.statistics.orderStatusCounts || this.statistics.orderStatusCounts.total === 0) {
      return 0;
    }
    return Math.round((this.statistics.orderStatusCounts.unpaid / this.statistics.orderStatusCounts.total) * 100);
  }

  // Gọi backend để xuất báo cáo
  exportReport(): void {
    const username = this.storageService.getUser()?.username || 'admin';
    if (this.statistics) {
      const payload: any = {
        title: 'Báo cáo tổng quan hệ thống',
        exportedBy: username,
        statistics: this.statistics
      };
      // Use sync endpoint to match dashboard exactly
      (this.reportApi as any).downloadDashboardReportSync(payload);
    } else {
      this.reportApi.downloadDashboardReport({ exportedBy: username, title: 'Báo cáo tổng quan hệ thống' });
    }
  }

  logout() {
    this.authService.logout().subscribe({
      next: data => {
        this.storageService.clean();
        // Chuyển hướng đến trang login
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error(err);
        // Ngay cả khi có lỗi, vẫn logout và chuyển hướng
        this.storageService.clean();
        this.router.navigate(['/login']);
      }
    });
  }
}
