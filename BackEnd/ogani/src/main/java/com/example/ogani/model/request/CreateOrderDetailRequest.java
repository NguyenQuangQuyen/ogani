package com.example.ogani.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOrderDetailRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống.")
    @Size(min = 1, max = 200, message = "Tên sản phẩm phải từ 1 đến 200 ký tự.")
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống.")
    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0.")
    private Long price;

    @NotNull(message = "Số lượng sản phẩm không được để trống.")
    @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn hoặc bằng 1.")
    private Integer quantity;

    // Constructor không tham số
    public CreateOrderDetailRequest() {
    }

    // Constructor đầy đủ tham số
    public CreateOrderDetailRequest(String name, Long price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getter và Setter cho name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter và Setter cho price
    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    // Getter và Setter cho quantity
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // Phương thức tính subTotal
    public Long getSubTotal() {
        if (price != null && quantity != null) {
            return price * quantity;
        }
        return 0L;
    }
}
