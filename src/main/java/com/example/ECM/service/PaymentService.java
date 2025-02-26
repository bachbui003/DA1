package com.example.ECM.service;

import com.example.ECM.dto.PaymentDTO;
import com.example.ECM.model.Payment;
import com.example.ECM.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Lưu thông tin thanh toán khi tạo đơn hàng
    public PaymentDTO createPayment(Long orderId, int amount) {
        Payment payment = Payment.builder()
                .paymentCode(UUID.randomUUID().toString()) // Mã giao dịch duy nhất
                .orderId(orderId)
                .amount(amount)
                .status("PENDING")
                .build();

        paymentRepository.save(payment);
        return convertToDTO(payment);
    }

    // Cập nhật trạng thái thanh toán từ VNPay
    public void updatePaymentStatus(String paymentCode, String status) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    // Lấy thông tin thanh toán theo mã
    public PaymentDTO getPaymentByCode(String paymentCode) {
        Optional<Payment> payment = paymentRepository.findByPaymentCode(paymentCode);
        return payment.map(this::convertToDTO).orElse(null);
    }

    // Chuyển đổi từ Entity sang DTO
    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .paymentCode(payment.getPaymentCode())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();
    }
}
