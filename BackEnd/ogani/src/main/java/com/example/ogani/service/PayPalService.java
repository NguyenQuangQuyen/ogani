package com.example.ogani.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {
    @Value("${paypal.client.id}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String clientSecret;
    @Value("${paypal.mode}")
    private String mode;
    @Value("${paypal.currency}")
    private String currency;
    @Value("${paypal.intent}")
    private String intent;
    @Value("${paypal.method}")
    private String method;
    @Value("${paypal.success.url}")
    private String successUrl;
    @Value("${paypal.cancel.url}")
    private String cancelUrl;

    private final String PAYPAL_API_SANDBOX = "https://api.sandbox.paypal.com";
    private final String PAYPAL_API_LIVE = "https://api.paypal.com";

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(mode) ? PAYPAL_API_SANDBOX : PAYPAL_API_LIVE;
    }

    public String getAccessToken() throws Exception {
        String url = getBaseUrl() + "/v1/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        JsonNode node = objectMapper.readTree(response.getBody());
        return node.get("access_token").asText();
    }

    public Map<String, Object> createOrder(Double total, String description) throws Exception {
        try {
            String url = getBaseUrl() + "/v2/checkout/orders";
            String accessToken = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Kiểm tra thêm tham số đầu vào
            if (total == null || total <= 0) {
                throw new IllegalArgumentException("Total must be a positive number");
            }

            System.out.println("Creating PayPal order with params: total=" + total + ", description=" + description + ", currency=" + currency);

            // Cấu trúc JSON đúng cho PayPal Orders API v2
            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("intent", intent);
            
            // Purchase units array
            List<Map<String, Object>> purchaseUnits = new ArrayList<>();
            Map<String, Object> purchaseUnit = new HashMap<>();
            
            // Amount object
            Map<String, Object> amount = new HashMap<>();
            amount.put("currency_code", currency);
            // Format với đúng 2 chữ số thập phân, bỏ bất kỳ dấu phân cách nào
            amount.put("value", String.format(java.util.Locale.US, "%.2f", total));
            
            purchaseUnit.put("amount", amount);
            purchaseUnit.put("description", description);
            purchaseUnits.add(purchaseUnit);
            
            orderRequest.put("purchase_units", purchaseUnits);
            
            // Application context
            Map<String, Object> applicationContext = new HashMap<>();
            applicationContext.put("return_url", successUrl);
            applicationContext.put("cancel_url", cancelUrl);
            applicationContext.put("brand_name", "Ogani Store");
            applicationContext.put("locale", "en-US");
            applicationContext.put("landing_page", "BILLING");
            applicationContext.put("user_action", "PAY_NOW");
            
            orderRequest.put("application_context", applicationContext);

            String requestJson = new ObjectMapper().writeValueAsString(orderRequest);
            System.out.println("REQUEST TO PAYPAL: " + requestJson);

            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
            
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                
                String responseBody = response.getBody();
                System.out.println("RESPONSE FROM PAYPAL: " + responseBody);
                
                if (responseBody == null || responseBody.isEmpty()) {
                    throw new RuntimeException("Empty response from PayPal API");
                }
                
                JsonNode node = objectMapper.readTree(responseBody);
                Map<String, Object> result = new HashMap<>();
                
                if (!node.has("id")) {
                    throw new RuntimeException("PayPal response missing 'id' field: " + responseBody);
                }
                
                result.put("id", node.get("id").asText());
                
                // Extract approval_url
                boolean foundApprovalUrl = false;
                for (JsonNode link : node.get("links")) {
                    if ("approve".equals(link.get("rel").asText())) {
                        result.put("approval_url", link.get("href").asText());
                        foundApprovalUrl = true;
                        break;
                    }
                }
                
                if (!foundApprovalUrl) {
                    throw new RuntimeException("PayPal response missing approval URL: " + responseBody);
                }
                
                return result;
            } catch (Exception e) {
                System.err.println("Error in PayPal API call: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("PayPal API error: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            System.err.println("Error in createOrder: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public JsonNode captureOrder(String orderId) throws Exception {
        try {
            String url = getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";
            String accessToken = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            System.out.println("Sending capture request to PayPal for order: " + orderId);
            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();
            
            System.out.println("PayPal capture response: " + responseBody);
            
            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("Empty response from PayPal capture API");
            }
            
            JsonNode resultNode = objectMapper.readTree(responseBody);
            
            // Kiểm tra trạng thái capture
            if (resultNode.has("status")) {
                String status = resultNode.get("status").asText();
                System.out.println("PayPal capture status: " + status);
                
                if (!"COMPLETED".equals(status)) {
                    System.err.println("PayPal capture not completed. Status: " + status);
                }
            } else {
                System.err.println("PayPal capture response missing status field");
            }
            
            return resultNode;
        } catch (Exception e) {
            System.err.println("Error in captureOrder: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
