package com.example.ogani.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.ogani.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    @Query(value ="Select * from Orders where user_id = :id order by id desc",nativeQuery = true)
    List<Order> getOrderByUser(long id);

    // Lấy danh sách đơn hàng theo user_id sử dụng JPA query method (không cần native SQL)
    // Hibernate sẽ tự map field userId -> column user_id
    List<Order> findByUserIdOrderByOrderIdDesc(Long userId);
    
    Order findByOrderId(String orderId);
}
