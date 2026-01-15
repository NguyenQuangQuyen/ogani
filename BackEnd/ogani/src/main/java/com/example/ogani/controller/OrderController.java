package com.example.ogani.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.example.ogani.entity.Order;
import com.example.ogani.entity.OrderDetail;
import com.example.ogani.model.request.CreatePaymentLinkRequestBody;
import com.example.ogani.model.request.CreateOrderRequest;
import com.example.ogani.model.request.UpdateStatusRequest;
import com.example.ogani.model.request.UpdateOrderRequest;
import com.example.ogani.model.response.MessageResponse;
import com.example.ogani.model.response.PayOSResponse;
import com.example.ogani.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.payos.PayOS;
import vn.payos.core.FileDownloadResponse;
import vn.payos.exception.APIException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.invoices.InvoicesInfo;
import vn.payos.model.webhooks.ConfirmWebhookResponse;
import vn.payos.model.webhooks.WebhookData;
import com.example.ogani.repository.OrderDetailRepository;
import com.example.ogani.model.response.DashboardStatisticsResponse;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final PayOS payOS;
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private com.example.ogani.service.ProductService productService;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public OrderController(PayOS payOS) {
        super();
        this.payOS = payOS;
    }

    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê dashboard - THEO THÁNG HIỆN TẠI")
    public ResponseEntity<?> getDashboardStatistics() {
        try {
            // ==================== CURRENT MONTH STATISTICS ====================
            Long totalSoldProducts = orderDetailRepository.getTotalSoldProductsCurrentMonth();
            Long totalRevenue = orderDetailRepository.getTotalRevenueCurrentMonth();
            Long totalRevenueFromPaid = orderDetailRepository.getTotalRevenueFromPaidOrdersCurrentMonth();
            
            // Lấy sản phẩm bán chạy nhất TRONG THÁNG HIỆN TẠI
            List<Object[]> topProducts = orderDetailRepository.getTopSellingProductsCurrentMonth();
            String bestProductName = "";
            Long bestProductQty = 0L;
            if (topProducts != null && !topProducts.isEmpty()) {
                Object[] top = topProducts.get(0);
                bestProductName = (String) top[0];
                bestProductQty = ((Number) top[1]).longValue();
            }
            
            // Lấy phân phối sản phẩm TRONG THÁNG HIỆN TẠI
            List<Object[]> distribution = orderDetailRepository.getProductSalesDistributionCurrentMonth();
            List<DashboardStatisticsResponse.ProductSalesInfo> productSales = new java.util.ArrayList<>();
            for (Object[] row : distribution) {
                String name = (String) row[0];
                Long qty = ((Number) row[1]).longValue();
                double percentage = totalSoldProducts > 0 ? (qty * 100.0 / totalSoldProducts) : 0;
                productSales.add(new DashboardStatisticsResponse.ProductSalesInfo(name, qty, percentage));
            }
            
            // ==================== 12-MONTH REVENUE OVERVIEW ====================
            List<Object[]> monthlyRevenueData = orderDetailRepository.getMonthlyRevenueForCurrentYear();
            
            // Khởi tạo mảng 12 tháng với revenue = 0
            Long[] monthlyRevenue = new Long[12];
            for (int i = 0; i < 12; i++) {
                monthlyRevenue[i] = 0L;
            }
            
            // Điền dữ liệu từ database vào mảng
            for (Object[] row : monthlyRevenueData) {
                int month = ((Number) row[0]).intValue(); // 1-12
                Long revenue = ((Number) row[1]).longValue();
                monthlyRevenue[month - 1] = revenue; // Array index 0-11
            }
            
            // Tạo danh sách monthly sales với tên tháng
            String[] monthNames = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", 
                                   "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
                                   "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
            List<DashboardStatisticsResponse.MonthlySaleInfo> monthlySales = new java.util.ArrayList<>();
            for (int i = 0; i < 12; i++) {
                monthlySales.add(new DashboardStatisticsResponse.MonthlySaleInfo(monthNames[i], monthlyRevenue[i]));
            }
            
            // ==================== ORDER STATUS & RECENT ORDERS (CURRENT MONTH) ====================
            List<Order> allOrders = orderService.getList();
            
            // Lọc đơn hàng theo tháng hiện tại
            java.time.LocalDate now = java.time.LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            
            List<Order> currentMonthOrders = allOrders.stream()
                .filter(o -> {
                    if (o.getCreatedDate() == null) return false;
                    java.time.LocalDate orderDate = o.getCreatedDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    return orderDate.getYear() == currentYear && orderDate.getMonthValue() == currentMonth;
                })
                .toList();
            
            long totalOrders = currentMonthOrders.size();
            long paidOrders = currentMonthOrders.stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getStatus()))
                .count();
            long unpaidOrders = currentMonthOrders.stream()
                .filter(o -> "UNPAID".equalsIgnoreCase(o.getStatus()) || "PENDING".equalsIgnoreCase(o.getStatus()))
                .count();

            // Lấy 5 đơn hàng gần nhất TRONG THÁNG HIỆN TẠI - CHỈ ĐƠN PAID
            java.util.List<DashboardStatisticsResponse.RecentOrderInfo> recentOrders = currentMonthOrders.stream()
                .filter(o -> "PAID".equalsIgnoreCase(o.getStatus()))  // CHỈ LẤY ĐƠN PAID
                .sorted((o1, o2) -> Long.compare(parseOrderIdToEpochMillis(o2.getOrderId()), parseOrderIdToEpochMillis(o1.getOrderId())))
                .map(o -> {
                    long created = parseOrderIdToEpochMillis(o.getOrderId());
                    long totalPriceCalculated = 0L;
                    if (o.getOrderDetails() != null && !o.getOrderDetails().isEmpty()) {
                        for (OrderDetail detail : o.getOrderDetails()) {
                            if (detail.getSubTotal() != null) {
                                totalPriceCalculated += detail.getSubTotal();
                            }
                        }
                    } else if (o.getPrice() != null && o.getQuantity() != null) {
                        totalPriceCalculated = o.getPrice() * o.getQuantity();
                    }
                    Long idValue;
                    try {
                        idValue = Long.parseLong(o.getOrderId());
                    } catch (NumberFormatException ex) {
                        idValue = null;
                    }
                    return new DashboardStatisticsResponse.RecentOrderInfo(
                        idValue,
                        o.getFirstname(),
                        o.getLastname(),
                        created,
                        totalPriceCalculated,
                        o.getStatus()
                    );
                })
                .limit(5)
                .toList();

            DashboardStatisticsResponse response = new DashboardStatisticsResponse();
            response.setTotalSoldProducts(totalSoldProducts);
            response.setTotalRevenue(totalRevenue);
            response.setTotalRevenueFromPaid(totalRevenueFromPaid);
            response.setBestSellingProductName(bestProductName);
            response.setBestSellingProductQuantity(bestProductQty);
            response.setProductSalesDistribution(productSales);
            response.setMonthlySales(monthlySales); // 12 tháng của năm hiện tại
            response.setOrderStatusCounts(new DashboardStatisticsResponse.OrderStatusCount(totalOrders, paidOrders, unpaidOrders));
            response.setRecentOrders(recentOrders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse("Lỗi khi lấy thống kê: " + e.getMessage()));
        }
    }

    private static long parseOrderIdToEpochMillis(String orderId) {
        if (orderId == null) {
            return 0L;
        }
        try {
            long seconds = Long.parseLong(orderId);
            return seconds * 1000L;
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    @GetMapping("/getall")
    @Operation(summary = "Lấy danh sách tất cả đơn hàng")
    public ResponseEntity<?> getList() {
        try {
            List<Order> list = orderService.getList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()));
        }
    }

    @PostMapping(path = "/create")
    public PayOSResponse<CreatePaymentLinkResponse> createPaymentLink(
            @RequestBody CreatePaymentLinkRequestBody requestBody) {
        try {
            // 1. Validate stock trước khi tạo đơn hàng (không trừ, chỉ kiểm tra)
            if (requestBody.getOrderDetails() != null && !requestBody.getOrderDetails().isEmpty()) {
                try {
                    productService.validateStock(requestBody.getOrderDetails());
                } catch (IllegalArgumentException e) {
                    return PayOSResponse.error(e.getMessage());
                }
            }
            
            final String productName = requestBody.getProductName();
            final String description = requestBody.getDescription();
            final String returnUrl = requestBody.getReturnUrl();
            final String cancelUrl = requestBody.getCancelUrl();
            final Long price = requestBody.getPrice();
            long orderCode = System.currentTimeMillis() / 1000;
            PaymentLinkItem item =
                    PaymentLinkItem.builder().name(productName).quantity(1).price(price).build();

            CreatePaymentLinkRequest paymentData =
                    CreatePaymentLinkRequest.builder()
                            .orderCode(orderCode)
                            .description(description)
                            .amount(price)
                            .item(item)
                            .returnUrl(returnUrl)
                            .cancelUrl(cancelUrl)
                            .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
            Order order = new Order();
            order.setOrderId(String.valueOf(orderCode));
            
            List<OrderDetail> details = new ArrayList<>();
            if (requestBody.getOrderDetails() != null && !requestBody.getOrderDetails().isEmpty()) {
                for (var detailRequest : requestBody.getOrderDetails()) {
                    long itemPrice = detailRequest.getPrice() != null ? detailRequest.getPrice() : 0L;
                    int qty = detailRequest.getQuantity() != null ? detailRequest.getQuantity() : 0;
                    
                    OrderDetail detail = new OrderDetail();
                    detail.setName(detailRequest.getName());
                    detail.setPrice(itemPrice);
                    detail.setQuantity(qty);
                    detail.setSubTotal(itemPrice * qty);
                    detail.setProductId(detailRequest.getProductId()); // Lưu productId để track
                    detail.setOrder(order);
                    details.add(detail);
                }
            }
            order.setOrderDetails(details);

            order.setUserId(requestBody.getUserId());
            order.setUsername(requestBody.getUsername());
            order.setProductId(requestBody.getProductId());
            order.setProductName(requestBody.getProductName());
            order.setPrice(requestBody.getPrice());
            order.setQuantity(1);
            order.setFirstname(requestBody.getFirstname());
            order.setLastname(requestBody.getLastname());
            order.setCountry(requestBody.getCountry());
            order.setState(requestBody.getState());
            order.setAddress(requestBody.getAddress());
            order.setPhone(requestBody.getPhone());
            order.setEmail(requestBody.getEmail());
            order.setTown(requestBody.getTown());
            order.setPostCode(requestBody.getPostCode() != null ? requestBody.getPostCode() : "");
            order.setNote(requestBody.getNote());
            order.setPrice(requestBody.getPrice());
            order.setStatus(String.valueOf(data.getStatus()));
            orderService.saveOrder(order);
            
            // Trừ tồn kho ngay khi tạo đơn hàng (bất kể PAID hay UNPAID)
            if (requestBody.getOrderDetails() != null && !requestBody.getOrderDetails().isEmpty()) {
                try {
                    System.out.println("=== DEBUG: Stock deduction for PayOS order " + orderCode + " ===");
                    for (var orderItem : requestBody.getOrderDetails()) {
                        System.out.println("Item: " + orderItem.getName() + ", ProductId: " + orderItem.getProductId() + ", Quantity: " + orderItem.getQuantity());
                    }
                    productService.deductStock(requestBody.getOrderDetails());
                    System.out.println("Stock deducted successfully for PayOS order: " + orderCode);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to deduct stock for PayOS order " + orderCode + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return PayOSResponse.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSResponse.error("fail");
        }
    }

    @GetMapping(path = "/{orderId}")
    public PayOSResponse<PaymentLink> getOrderById(@PathVariable("orderId") long orderId) {
        try {
            PaymentLink order = payOS.paymentRequests().get(orderId);
            return PayOSResponse.success("ok", order);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSResponse.error(e.getMessage());
        }
    }

    

    @GetMapping("/user-id/{userId}")
    @Operation(summary = "Lấy danh sách đơn hàng của người dùng theo user_id")
    public ResponseEntity<?> getListByUserId(@PathVariable("userId") Long userId) {
        try {
            List<Order> list = orderService.getOrderByUserId(userId);

            if (list.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Không tìm thấy đơn hàng cho user_id: " + userId));
            }

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Lỗi khi lấy danh sách đơn hàng theo user_id: " + e.getMessage()));
        }
    }

    @PutMapping(path = "/{orderId}")
    public PayOSResponse<PaymentLink> cancelOrder(@PathVariable("orderId") long orderId) {
        try {
            PaymentLink order = payOS.paymentRequests().cancel(orderId, "change my mind");
            return PayOSResponse.success("ok", order);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSResponse.error(e.getMessage());
        }
    }

    @PostMapping(path = "/confirm-webhook")
    public PayOSResponse<ConfirmWebhookResponse> confirmWebhook(
            @RequestBody Map<String, String> requestBody) {
        try {
            ConfirmWebhookResponse result = payOS.webhooks().confirm(requestBody.get("webhookUrl"));
            return PayOSResponse.success("ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSResponse.error(e.getMessage());
        }
    }

    @GetMapping(path = "/{orderId}/invoices")
    public PayOSResponse<InvoicesInfo> retrieveInvoices(@PathVariable("orderId") long orderId) {
        try {
            InvoicesInfo invoicesInfo = payOS.paymentRequests().invoices().get(orderId);
            return PayOSResponse.success("ok", invoicesInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return PayOSResponse.error(e.getMessage());
        }
    }

    @GetMapping(path = "/{orderId}/invoices/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(
            @PathVariable("orderId") long orderId, @PathVariable("invoiceId") String invoiceId) {
        try {
            FileDownloadResponse invoiceFile =
                    payOS.paymentRequests().invoices().download(invoiceId, orderId);

            if (invoiceFile == null || invoiceFile.getData() == null) {
                return ResponseEntity.status(404).body(PayOSResponse.error("invoice not found or empty"));
            }

            ByteArrayResource resource = new ByteArrayResource(invoiceFile.getData());

            HttpHeaders headers = new HttpHeaders();
            String contentType =
                    invoiceFile.getContentType() == null
                            ? MediaType.APPLICATION_PDF_VALUE
                            : invoiceFile.getContentType();
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + invoiceFile.getFilename() + "\"");
            if (invoiceFile.getSize() != null) {
                headers.setContentLength(invoiceFile.getSize());
            }

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (APIException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(PayOSResponse.error(e.getErrorDesc().orElse(e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(PayOSResponse.error(e.getMessage()));
        }
    }

    @PostMapping(path = "/payos_transfer_handler")
    public PayOSResponse<WebhookData> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        try {
            // Verify webhook từ PayOS
            WebhookData data = payOS.webhooks().verify(body);
            System.out.println("=== PayOS Webhook received ===");
            System.out.println("WebhookData: " + data);
            
            // FIX: Lấy orderCode trực tiếp từ data (không qua .getData())
            Long orderCode = data.getOrderCode();
            System.out.println("OrderCode: " + orderCode);
            
            // FIX: WebhookData không có getStatus(). 
            // Khi Webhook bắn về và verify thành công nghĩa là giao dịch đã thanh toán.
            String status = "PAID"; 
            
            if (orderCode != null) {
                String orderId = String.valueOf(orderCode);
                Order order = orderService.getStatusById(orderId);
                System.out.println("Found order: " + (order != null ? order.getOrderId() : "null"));
                
                if (order != null) {
                    String previousStatus = order.getStatus();
                    System.out.println("Previous status: " + previousStatus);
                    
                    // Cập nhật status thành PAID vì nhận được webhook thành công
                    order.setStatus(status);
                    orderService.saveOrder(order);
                    System.out.println("Order " + orderId + " status updated to " + status);
                    
                    // Debug: Log order details
                    System.out.println("Order details count: " + (order.getOrderDetails() != null ? order.getOrderDetails().size() : 0));
                    if (order.getOrderDetails() != null) {
                        for (OrderDetail od : order.getOrderDetails()) {
                            System.out.println("  - Detail: " + od.getName() + ", productId=" + od.getProductId() + ", qty=" + od.getQuantity());
                        }
                    }
                    
                    // Note: Stock đã được trừ ngay khi tạo đơn hàng (trong createPaymentLink hoặc createCodOrder)
                    // Không cần trừ lại ở đây để tránh trừ 2 lần
                    System.out.println("Stock was already deducted at order creation time - no action needed here");
                } else {
                    System.out.println("Order not found with orderId: " + orderId);
                }
            }
            
            return PayOSResponse.success("Webhook delivered", data);
        } catch (Exception e) {
            System.err.println("PayOS Webhook Error: " + e.getMessage());
            e.printStackTrace();
            return PayOSResponse.error(e.getMessage());
        }
    }

    @GetMapping("/get_status/{order_id}")
    public String getStatus(@PathVariable("order_id") String orderId) {
        Order order = orderService.getStatusById(orderId);
        String status = order.getStatus();
        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy đơn hàng với order_id = " + orderId
            );
        }
        return status;
    }
    
    @PutMapping("/update_status/{order_id}")
    public Order updateStatus(@PathVariable("order_id") String orderId, @RequestBody UpdateStatusRequest request) {
        System.out.println("Đang tìm kiếm order_id: " + orderId);
        Order order = orderService.getStatusById(orderId);
        if (order == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy đơn hàng với order_id = " + orderId
            );
        }
        
        String previousStatus = order.getStatus();
        String newStatus = request.getStatus();
        
        order.setStatus(newStatus);
        Order savedOrder = orderService.saveOrder(order);
        
        // Note: Stock đã được trừ ngay khi tạo đơn hàng (trong createPaymentLink hoặc createCodOrder)
        // Không cần trừ lại ở đây để tránh trừ 2 lần
        System.out.println("Order " + orderId + " status updated from " + previousStatus + " to " + newStatus);
        
        return savedOrder;
    }

    @PutMapping("/update/{order_id}")
    @Operation(summary = "Cập nhật thông tin đơn hàng theo order_id")
    public ResponseEntity<?> updateOrder(
            @PathVariable("order_id") String orderId,
            @RequestBody UpdateOrderRequest request) {
        try {
            Order order = orderService.getStatusById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Không tìm thấy đơn hàng với order_id = " + orderId));
            }

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

            Order saved = orderService.saveOrder(order);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Lỗi khi cập nhật đơn hàng: " + e.getMessage()));
        }
    }

    @PostMapping(path = "/create_cod")
    @Operation(summary = "Tạo đơn hàng COD (mặc định UNPAID)")
    public ResponseEntity<?> createCodOrder(@RequestBody CreateOrderRequest request) {
        try {
            // 1. Validate stock trước khi tạo đơn hàng
            if (request.getOrderDetails() != null && !request.getOrderDetails().isEmpty()) {
                try {
                    productService.validateStock(request.getOrderDetails());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(new MessageResponse(e.getMessage()));
                }
            }
            
            long orderCode = System.currentTimeMillis() / 1000;
            Order order = new Order();
            order.setOrderId(String.valueOf(orderCode));

            long totalPrice = 0L;
            int totalQuantity = 0;
            String productName;
            List<OrderDetail> details = new ArrayList<>();

            if (request.getOrderDetails() != null && !request.getOrderDetails().isEmpty()) {
                // Liệt kê chi tiết tên các sản phẩm
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < request.getOrderDetails().size(); i++) {
                    var item = request.getOrderDetails().get(i);
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(item.getName());
                    
                    long price = item.getPrice() != null ? item.getPrice() : 0L;
                    int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                    
                    OrderDetail detail = new OrderDetail();
                    detail.setName(item.getName());
                    detail.setPrice(price);
                    detail.setQuantity(qty);
                    detail.setSubTotal(price * qty);
                    detail.setProductId(item.getProductId()); // Lưu productId để track
                    detail.setOrder(order);
                    details.add(detail);
                    
                    totalPrice += price * qty;
                    totalQuantity += qty;
                }
                productName = sb.toString();
                
                // Giới hạn độ dài chuỗi để tránh lỗi database (thường là 255 ký tự)
                if (productName.length() > 250) {
                    productName = productName.substring(0, 247) + "...";
                }
            } else {
                productName = "Order";
            }

            order.setUserId(request.getUserId());
            order.setUsername(request.getUsername());
            order.setFirstname(request.getFirstname());
            order.setLastname(request.getLastname());
            order.setCountry(request.getCountry());
            order.setState(request.getState());
            order.setAddress(request.getAddress());
            order.setPhone(request.getPhone());
            order.setEmail(request.getEmail());
            order.setTown(request.getTown());
            order.setPostCode(request.getPostCode() != null ? request.getPostCode() : "");
            order.setNote(request.getNote());

            order.setProductName(productName);
            order.setPrice(totalPrice);
            order.setQuantity(totalQuantity > 0 ? totalQuantity : 1);
            order.setStatus("UNPAID");
            
            order.setOrderDetails(details);

            Order saved = orderService.saveOrder(order);
            
            // 2. Trừ tồn kho sau khi lưu đơn hàng thành công
            if (request.getOrderDetails() != null && !request.getOrderDetails().isEmpty()) {
                try {
                    // Debug: Log chi tiết order details
                    System.out.println("=== DEBUG: Stock deduction for order " + orderCode + " ===");
                    for (var item : request.getOrderDetails()) {
                        System.out.println("Item: " + item.getName() + ", ProductId: " + item.getProductId() + ", Quantity: " + item.getQuantity());
                    }
                    
                    productService.deductStock(request.getOrderDetails());
                    System.out.println("Stock deducted successfully for COD order: " + orderCode);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to deduct stock for order " + orderCode + ": " + e.getMessage());
                    e.printStackTrace();
                    // Không rollback đơn hàng, chỉ log warning
                }
            }
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Lỗi khi tạo đơn COD: " + e.getMessage()));
        }
    }

    @DeleteMapping(path = "/delete/{order_id}")
    @Operation(summary = "Xóa hoàn toàn đơn hàng theo order_id")
    public ResponseEntity<?> deleteOrder(@PathVariable("order_id") String orderId) {
        try {
            Order order = orderService.getStatusById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Không tìm thấy đơn hàng với order_id = " + orderId));
            }
            orderService.deleteOrder(order);
            return ResponseEntity.ok(new MessageResponse("Đã xóa đơn hàng " + orderId + " thành công"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Lỗi khi xóa đơn hàng: " + e.getMessage()));
        }
    }
}
