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
}
