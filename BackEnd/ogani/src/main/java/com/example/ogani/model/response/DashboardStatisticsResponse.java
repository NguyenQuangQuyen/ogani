package com.example.ogani.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatisticsResponse {
    private Long totalSoldProducts;      // Tổng số sản phẩm đã bán
    private Long totalRevenue;           // Tổng doanh thu (tất cả đơn)
    private Long totalRevenueFromPaid;   // Doanh thu từ đơn đã thanh toán
    private String bestSellingProductName;
    private Long bestSellingProductQuantity;
    private List<ProductSalesInfo> productSalesDistribution;
    private OrderStatusCount orderStatusCounts;
    private List<RecentOrderInfo> recentOrders;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSalesInfo {
        private String name;
        private Long quantity;
        private Double percentage;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderStatusCount {
        private Long total;
        private Long paid;
        private Long unpaid;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentOrderInfo {
        private Long id;
        private String firstname;
        private String lastname;
        private Long createdDate;
        private Long totalPrice;
        private String status;
    }
}
