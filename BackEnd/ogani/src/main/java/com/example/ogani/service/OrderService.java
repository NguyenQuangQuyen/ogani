package com.example.ogani.service;

import java.util.List;

import com.example.ogani.entity.Order;
import com.example.ogani.model.request.CreateOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;

public interface OrderService {
    /**
     * Lấy danh sách tất cả đơn hàng.
     * 
     * @return Danh sách các đơn hàng.
     */
    List<Order> getList();

    /**
     * Lấy danh sách đơn hàng của một người dùng cụ thể.
     * 
     * @param username Tên người dùng cần lấy danh sách đơn hàng.
     * @return Danh sách đơn hàng của người dùng.
     */
    List<Order> getOrderByUser(String username);

    /**
     * Lấy danh sách đơn hàng theo user_id.
     *
     * Dùng cho trường hợp frontend gửi trực tiếp userId,
     * đảm bảo chỉ lấy đúng đơn hàng của user đó.
     */
    List<Order> getOrderByUserId(Long userId);

    Order saveOrder(Order order);
    Order getStatusById(String id);
    void deleteOrder(Order order);
}
