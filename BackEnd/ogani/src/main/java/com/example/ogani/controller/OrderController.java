package com.example.ogani.controller;

import java.util.List;
import java.util.Map;

import com.example.ogani.entity.Order;
import com.example.ogani.model.request.CreatePaymentLinkRequestBody;
import com.example.ogani.model.request.UpdateStatusRequest;
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

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final PayOS payOS;
    @Autowired
    private OrderService orderService;

    public OrderController(PayOS payOS) {
        super();
        this.payOS = payOS;
    }

    @GetMapping("/")
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
            @RequestBody CreatePaymentLinkRequestBody RequestBody) {
        try {
            final String productName = RequestBody.getProductName();
            final String description = RequestBody.getDescription();
            final String returnUrl = RequestBody.getReturnUrl();
            final String cancelUrl = RequestBody.getCancelUrl();
            final Long price = RequestBody.getPrice();
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
            order.setProductId(RequestBody.getProductId());
            order.setProductName(RequestBody.getProductName());
            order.setPrice(RequestBody.getPrice());
            order.setQuantity(1);
            order.setFirstname(RequestBody.getFirstname());
            order.setLastname(RequestBody.getLastname());
            order.setCountry(RequestBody.getCountry());
            order.setState(RequestBody.getState());
            order.setAddress(RequestBody.getAddress());
            order.setPhone(RequestBody.getPhone());
            order.setEmail(RequestBody.getEmail());
            order.setTown(RequestBody.getTown());
            order.setPostCode(RequestBody.getPostCode() != null ? RequestBody.getPostCode() : "");
            order.setNote(RequestBody.getNote());
            order.setPrice(RequestBody.getPrice());
            order.setStatus(String.valueOf(data.getStatus()));
            orderService.saveOrder(order);
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

    @GetMapping("/user")
    @Operation(summary = "Lấy danh sách đơn hàng của người dùng theo username")
    public ResponseEntity<?> getListByUser(@RequestParam("username") String username) {
        try {
            List<Order> list = orderService.getOrderByUser(username);

            if (list.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Không tìm thấy đơn hàng cho người dùng: " + username));
            }

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()));
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
            System.out.println("PayOS Webhook received: " + data);
            
            // Lấy orderCode và status từ webhook data
            Long orderCode = data.getData().getOrderCode();
            String status = data.getData().getStatus();
            
            if (orderCode != null) {
                String orderId = String.valueOf(orderCode);
                Order order = orderService.getStatusById(orderId);
                
                if (order != null) {
                    // Cập nhật status dựa trên trạng thái từ PayOS
                    // PayOS status: PAID, CANCELLED, PENDING, etc.
                    if ("PAID".equals(status)) {
                        order.setStatus("PAID");
                        orderService.saveOrder(order);
                        System.out.println("Order " + orderId + " status updated to PAID");
                    } else if ("CANCELLED".equals(status)) {
                        order.setStatus("CANCELLED");
                        orderService.saveOrder(order);
                        System.out.println("Order " + orderId + " status updated to CANCELLED");
                    } else {
                        // Có thể có các status khác như PENDING, EXPIRED, etc.
                        order.setStatus(status);
                        orderService.saveOrder(order);
                        System.out.println("Order " + orderId + " status updated to " + status);
                    }
                } else {
                    System.out.println("Order not found with orderId: " + orderId);
                }
            }
            
            return PayOSResponse.success("Webhook delivered", data);
        } catch (Exception e) {
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
        order.setStatus(request.getStatus());
        return orderService.saveOrder(order);
    }
}