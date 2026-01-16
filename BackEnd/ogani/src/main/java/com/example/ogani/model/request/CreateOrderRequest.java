package com.example.ogani.model.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    private Long userId;

    @NotBlank(message = "Username is required.")
    private String username;

    @NotBlank(message = "First name is required.")
    @Size(max = 50, message = "First name must not exceed 50 characters.")
    private String firstname;

    @NotBlank(message = "Last name is required.")
    @Size(max = 50, message = "Last name must not exceed 50 characters.")
    private String lastname;

    @NotBlank(message = "Country is required.")
    private String country;

    @NotBlank(message = "State is required.")
    private String state;

    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits.")
    private String phone;

    // Make postCode optional by removing validations
    private String postCode;

    @Email(message = "Invalid email format.")
    private String email;

    private String note;

    @NotEmpty(message = "Order details must not be empty.")
    private List<CreateOrderDetailRequest> orderDetails;

    @NotBlank(message = "Town is required.")
    private String town;
    
    private String paymentMethod; // COD, BANK, PAYOS, etc.
}
