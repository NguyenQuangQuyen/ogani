package com.example.ogani.service;

import java.util.List;

import com.example.ogani.entity.Order;
import com.example.ogani.model.request.CreateOrderRequest;

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

    Order saveOrder(Order order);
}
