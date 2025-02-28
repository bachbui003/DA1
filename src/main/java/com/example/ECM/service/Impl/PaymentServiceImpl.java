package com.example.ECM.service.Impl;

import com.example.ECM.model.Order;
import com.example.ECM.model.Payment;
import com.example.ECM.model.PaymentStatus;
import com.example.ECM.repository.OrderRepository;
import com.example.ECM.repository.PaymentRepository;
import com.example.ECM.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(order.getUser());
        payment.setAmount(order.getTotalPrice());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    public void updatePaymentStatus(String transactionId, PaymentStatus status, String vnpTransactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với mã: " + transactionId));

        payment.setTransactionId(vnpTransactionId); // Gán mã giao dịch VNPay trả về
        payment.setPaymentStatus(status);
        paymentRepository.save(payment);

        if (status == PaymentStatus.SUCCESS) {
            Long orderId = payment.getOrder().getId();
            orderRepository.deleteById(orderId);
            System.out.println("Xóa đơn hàng có ID: " + orderId);
            System.out.println("Mã giao dịch VNPay: " + vnpTransactionId);
        }
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId).orElse(null);
    }
}