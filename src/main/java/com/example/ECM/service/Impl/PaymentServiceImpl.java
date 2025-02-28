package com.example.ECM.service.Impl;

import com.example.ECM.model.Order;
import com.example.ECM.model.Payment;
import com.example.ECM.repository.PaymentRepository;
import com.example.ECM.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(order.getUser()); // Gán user từ order
        payment.setAmount(order.getTotalPrice());
        payment.setPaymentStatus("PENDING");
        payment.setStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentCode(UUID.randomUUID().toString()); // Thêm mã thanh toán
        return paymentRepository.save(payment);
    }



    @Override
    public void updatePaymentStatus(String transactionId, String status) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setPaymentStatus(status);
        paymentRepository.save(payment);
    }
}
