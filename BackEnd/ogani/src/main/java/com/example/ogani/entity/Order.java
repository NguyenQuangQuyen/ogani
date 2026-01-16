package com.example.ogani.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(nullable = false, length = 50)
    private String orderId;

    private Long userId;

    private String username;

    @Column(nullable = false, length = 50)
    private String firstname;

    @Column(nullable = false, length = 50)
    private String lastname;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 500)
    private String note;

    @Column(nullable = false, length = 50)
    private String town;

    @Column(nullable = false, length = 20)
    private String postCode;

    @Column(nullable = false, length = 100)
    private String email;

    private String productName;

    private Long productId;

    private String status;
    
    @Column(length = 20)
    private String paymentMethod; // COD, BANK, PAYOS, etc.

    private Long price;

    private Integer quantity;
    
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    // Tự động set created_date khi tạo đơn hàng mới
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = new java.util.Date();
        }
    }
}
