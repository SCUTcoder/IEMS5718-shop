package com.iems5718.shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderid")
    private Long orderId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "merchant_email", nullable = false, length = 255)
    private String merchantEmail;

    @Column(name = "salt", nullable = false, length = 128)
    private String salt;

    @Column(name = "digest", nullable = false, length = 128)
    private String digest;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "paypal_order_id", length = 255)
    private String paypalOrderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
