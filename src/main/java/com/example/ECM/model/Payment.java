package com.example.ECM.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentCode; // Mã giao dịch VNPay

    @Column(nullable = false)
    private Long orderId; // ID đơn hàng

    @Column(nullable = false)
    private int amount; // Số tiền thanh toán

    @Column(nullable = false)
    private String status; // Trạng thái: PENDING, SUCCESS, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
