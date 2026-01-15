package com.example.ogani.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.ogani.entity.OrderDetail;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {
    
    // Tổng số sản phẩm đã bán (SUM của cột quantity)
    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od")
    Long getTotalSoldProducts();
    
    // Tổng doanh thu (SUM của cột price)
    @Query("SELECT COALESCE(SUM(od.subTotal), 0) FROM OrderDetail od")
    Long getTotalRevenue();
    
    // Tổng doanh thu từ đơn hàng đã thanh toán (SUM của cột price từ đơn PAID)
    @Query("SELECT COALESCE(SUM(od.price), 0) FROM OrderDetail od WHERE od.order.status = 'PAID'")
    Long getTotalRevenueFromPaidOrders();
    
    // Sản phẩm bán chạy nhất (sản phẩm có quantity cao nhất)
    @Query("SELECT od.name, SUM(od.quantity) as totalQty FROM OrderDetail od GROUP BY od.name ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProducts();
    
    // Số lượng bán theo từng sản phẩm, sắp xếp theo quantity giảm dần
    @Query("SELECT od.name, SUM(od.quantity) as totalQty FROM OrderDetail od GROUP BY od.name ORDER BY totalQty DESC")
    List<Object[]> getProductSalesDistribution();
    
    // ==================== CURRENT MONTH STATISTICS ====================
    // CHỈ TÍNH ĐơN HÀNG ĐÃ THANH TOÁN (PAID)
    
    // Tổng số sản phẩm đã bán TRONG THÁNG HIỆN TẠI (CHỈ ĐƠN PAID)
    @Query(value = "SELECT COALESCE(SUM(od.quantity), 0) FROM order_details od " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE o.status = 'PAID' " +
                   "AND EXTRACT(YEAR FROM o.created_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                   "AND EXTRACT(MONTH FROM o.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)", 
           nativeQuery = true)
    Long getTotalSoldProductsCurrentMonth();
    
    // Tổng doanh thu TRONG THÁNG HIỆN TẠI (CHỈ ĐƠN PAID)
    @Query(value = "SELECT COALESCE(SUM(od.sub_total), 0) FROM order_details od " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE o.status = 'PAID' " +
                   "AND EXTRACT(YEAR FROM o.created_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                   "AND EXTRACT(MONTH FROM o.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)", 
           nativeQuery = true)
    Long getTotalRevenueCurrentMonth();
    
    // Tổng doanh thu từ đơn hàng đã thanh toán TRONG THÁNG HIỆN TẠI
    @Query(value = "SELECT COALESCE(SUM(od.sub_total), 0) FROM order_details od " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE o.status = 'PAID' " +
                   "AND EXTRACT(YEAR FROM o.created_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                   "AND EXTRACT(MONTH FROM o.created_date) = EXTRACT(MONTH FROM CURRENT_DATE)", 
           nativeQuery = true)
    Long getTotalRevenueFromPaidOrdersCurrentMonth();
    
    // Sản phẩm bán chạy nhất TRONG THÁNG HIỆN TẠI (CHỈ ĐƠN PAID)
    @Query(value = "SELECT od.name, SUM(od.quantity) as totalQty FROM order_details od " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE o.status = 'PAID' " +
                   "AND EXTRACT(YEAR FROM o.created_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                   "AND EXTRACT(MONTH FROM o.created_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                   "GROUP BY od.name ORDER BY totalQty DESC", 
           nativeQuery = true)
    List<Object[]> getTopSellingProductsCurrentMonth();
    
    // Phân phối sản phẩm TRONG THÁNG HIỆN TẠI (CHỈ ĐƠN PAID)
    @Query(value = "SELECT od.name, SUM(od.quantity) as totalQty FROM order_details od " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE o.status = 'PAID' " +
                   "AND EXTRACT(YEAR FROM o.created_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                   "AND EXTRACT(MONTH FROM o.created_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                   "GROUP BY od.name ORDER BY totalQty DESC", 
           nativeQuery = true)
    List<Object[]> getProductSalesDistributionCurrentMonth();
    
    // ==================== 12-MONTH REVENUE OVERVIEW ====================
    
    // Doanh thu theo từng tháng trong năm hiện tại (12 tháng) - CHỈ ĐƠN PAID
    @Query(value = "SELECT EXTRACT(MONTH FROM o.created_date) as month, " +
                   "COALESCE(SUM(od.sub_total), 0) as revenue " +
                   "FROM order_details od " +
                   "JOIN orders o ON od.order_id = o.order_id " +
                   "WHERE o.status = 'PAID' " +
                   "AND EXTRACT(YEAR FROM o.created_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
                   "GROUP BY EXTRACT(MONTH FROM o.created_date) " +
                   "ORDER BY month", 
           nativeQuery = true)
    List<Object[]> getMonthlyRevenueForCurrentYear();
}
