package com.example.ogani.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class CreatePaymentLinkRequestBody {
    private String username;
    private Long userId;
    private String firstname;
    private String lastname;
    private String country;
    private String state;
    private String address;
    private String phone;
    private String email;
    private String town;
    private String postCode;
    private String note;
    private String orderId;
    private String description;
    private String returnUrl;
    private String productName;
    private Long productId;
    private Long price;
    private String cancelUrl;
    private List<CreateOrderDetailRequest> orderDetails;
}
