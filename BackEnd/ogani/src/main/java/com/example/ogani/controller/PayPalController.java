package com.example.ogani.controller;

import com.example.ogani.service.PayPalService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/paypal")
@CrossOrigin(origins = {"http://localhost:4200"})
public class PayPalController {
    @Autowired
    private PayPalService payPalService;

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> pay(@RequestBody Map<String, Object> data) {
        Double total;
        String description;
        
        System.out.println("Received request: " + data);
        
        try {
            // Cải thiện xử lý chuyển đổi số
            Object totalObj = data.get("total");
            if (totalObj == null) {
                throw new IllegalArgumentException("Missing 'total' parameter");
            }
            
            // Xử lý theo từng kiểu dữ liệu có thể có
            if (totalObj instanceof Double) {
                total = (Double) totalObj;
            } else if (totalObj instanceof Integer) {
                total = ((Integer) totalObj).doubleValue();
            } else if (totalObj instanceof String) {
                total = Double.parseDouble((String) totalObj);
            } else if (totalObj instanceof Long) {
                total = ((Long) totalObj).doubleValue();
            } else if (totalObj instanceof Float) {
                total = ((Float) totalObj).doubleValue();
            } else {
                total = Double.valueOf(totalObj.toString());
            }
            
            System.out.println("Parsed total value: " + total);
            
            description = (String) data.get("description");
            if (description == null) {
                description = "Thanh toán đơn hàng Ogani";
            }
        } catch (Exception e) {
            System.err.println("Invalid request data: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid request data: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            if (total <= 0) {
                throw new IllegalArgumentException("Total must be greater than 0");
            }
            
            System.out.println("Creating PayPal order with total: " + total);
            Map<String, Object> result = payPalService.createOrder(total, description);
            
            // Validate result
            if (result == null || !result.containsKey("id")) {
                throw new RuntimeException("PayPal API returned invalid order data");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("approval_url", result.get("approval_url"));
            response.put("orderId", result.get("id"));
            System.out.println("PAYPAL ORDER CREATED: " + response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("PayPal API error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "PayPal API error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/capture")
    public ResponseEntity<?> capture(@RequestBody Map<String, String> data) {
        String orderId = data.get("orderId");
        System.out.println("Capturing PayPal order: " + orderId);
        
        if (orderId == null || orderId.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing orderId parameter");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            JsonNode captureResult = payPalService.captureOrder(orderId);
            System.out.println("PayPal capture result: " + captureResult);
            return ResponseEntity.ok(captureResult);
        } catch (Exception e) {
            System.err.println("PayPal capture error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "PayPal capture error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
