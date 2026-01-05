package com.example.ogani.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.Order;
import com.example.ogani.entity.User;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.CreateOrderRequest;
import com.example.ogani.repository.OrderRepository;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo đơn hàng (1 order = 1 sản phẩm)
     */
//    @Override
//    public void placeOrder(CreateOrderRequest request) {
//
//        // 1. Lấy user
//        User user = userRepository.findByUsername(request.getUsername())
//                .orElseThrow(() ->
//                        new NotFoundException("Người dùng không tồn tại: " + request.getUsername()));
//
//        // 2. Validate sản phẩm
//        validateProduct(request);
//
//        // 3. Tạo Order
//        Order order = new Order();
//        order.setUserId(user.getId());
//        order.setUsername(user.getUsername());
//
//        mapCustomerInfo(order, request);
//        mapProductInfo(order, request);
//
//        // 4. Tính tổng tiền
//        long totalPrice = request.getPrice() * request.getQuantity();
//        order.setTotalPrice(totalPrice);
//
//        // 5. Trạng thái mặc định
//        order.setStatus("UNPAID");
//
//        // 6. Lưu DB
//        orderRepository.save(order);
//    }

    /**
     * Mapping thông tin khách hàng
     */
    private void mapCustomerInfo(Order order, CreateOrderRequest request) {
        order.setFirstname(request.getFirstname());
        order.setLastname(request.getLastname());
        order.setCountry(request.getCountry());
        order.setAddress(request.getAddress());
        order.setTown(request.getTown());
        order.setState(request.getState());
        order.setPostCode(request.getPostCode() != null ? request.getPostCode() : "");
        order.setEmail(request.getEmail());
        order.setPhone(request.getPhone());
        order.setNote(request.getNote());
    }

    /**
     * Mapping thông tin sản phẩm
     */
//    private void mapProductInfo(Order order, CreateOrderRequest request) {
//        order.setProductId(request.getProductId());
//        order.setProductName(request.getProductName());
//        order.setPrice(request.getPrice());
//        order.setQuantity(request.getQuantity());
//    }

    /**
     * Validate sản phẩm
     */
//    private void validateProduct(CreateOrderRequest request) {
//        if (request.getProductName() == null || request.getProductName().isEmpty()) {
//            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
//        }
//        if (request.getPrice() == null || request.getPrice() < 0) {
//            throw new IllegalArgumentException("Giá sản phẩm phải >= 0");
//        }
//        if (request.getQuantity() == null || request.getQuantity() <= 0) {
//            throw new IllegalArgumentException("Số lượng sản phẩm phải >= 1");
//        }
//    }

    /**
     * Lấy danh sách tất cả đơn hàng
     */
    @Override
    public List<Order> getList() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderId"));
    }

    /**
     * Lấy đơn hàng theo user
     */
    @Override
    public List<Order> getOrderByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new NotFoundException("Người dùng không tồn tại: " + username));

        List<Order> orders = orderRepository.getOrderByUser(user.getId());

        if (orders == null || orders.isEmpty()) {
            throw new NotFoundException("Không tìm thấy đơn hàng cho user: " + username);
        }

        return orders;
    }

    /**
     * Lấy đơn hàng theo user_id (dùng khi frontend gửi trực tiếp userId)
     */
    @Override
    public List<Order> getOrderByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderIdDesc(userId);

        if (orders == null || orders.isEmpty()) {
            throw new NotFoundException("Không tìm thấy đơn hàng cho user_id: " + userId);
        }

        return orders;
    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order getStatusById(String id) {
        return orderRepository.findByOrderId(id);
    }

    @Override
    public void deleteOrder(Order order) {
        orderRepository.delete(order);
    }
}
