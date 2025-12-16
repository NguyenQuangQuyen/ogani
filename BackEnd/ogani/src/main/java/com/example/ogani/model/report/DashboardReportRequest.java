package com.example.ogani.model.report;

import java.util.List;

public class DashboardReportRequest {
    public String title;
    public String exportedBy;
    public String timeRangeFrom; // optional
    public String timeRangeTo;   // optional
    public DashboardStatisticsDto statistics;

    public static class DashboardStatisticsDto {
        public int totalSoldProducts;
        public long totalRevenue;
        public BestProductDto bestSellingProduct;
        public List<MonthlySaleDto> monthlySales;
        public List<ProductDistributionDto> productSalesDistribution;
        public List<RecentOrderDto> recentOrders;
        public OrderStatusCountsDto orderStatusCounts;
    }

    public static class BestProductDto { public String name; public int quantity; }
    public static class MonthlySaleDto { public String month; public long revenue; }
    public static class ProductDistributionDto { public String name; public int quantity; public int percentage; }
    public static class RecentOrderDto {
        public Long id; public String firstname; public String lastname; public String createdDate;
        public long totalPrice; public String status;
    }
    public static class OrderStatusCountsDto { public int total; public int paid; public int unpaid; }
}


