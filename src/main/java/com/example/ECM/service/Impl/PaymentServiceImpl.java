package com.example.ECM.service.Impl;

import com.example.ECM.model.Order;
import com.example.ECM.model.Payment;
import com.example.ECM.model.PaymentStatus;
import com.example.ECM.repository.OrderRepository;
import com.example.ECM.repository.PaymentRepository;
import com.example.ECM.service.EmailService;
import com.example.ECM.service.PaymentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EmailService emailService;

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
    @Transactional
    public void updatePaymentStatus(String transactionId, PaymentStatus status, String vnpTransactionStatus, String vnpTxnRef, Long vnpAmount) {
        System.out.println("Bắt đầu cập nhật thanh toán...");
        System.out.println("TransactionId: " + transactionId);
        System.out.println("VNPay Transaction Status: " + vnpTransactionStatus);
        System.out.println("VNP Transaction Ref (Order): " + vnpTxnRef);
        System.out.println("VNP Amount: " + vnpAmount);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với mã: " + transactionId));

        if ("00".equals(vnpTransactionStatus)) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            System.out.println("Cập nhật trạng thái thanh toán thành SUCCESS");
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            System.out.println("Cập nhật trạng thái thanh toán thành FAILED");
        }

        payment.setVnpTransactionNo(vnpTxnRef);
        payment.setAmount(BigDecimal.valueOf(vnpAmount));
        paymentRepository.save(payment);
        paymentRepository.flush(); // Đảm bảo dữ liệu được đẩy vào DB ngay lập tức
        System.out.println("Payment Status After Save: " + paymentRepository.findById(payment.getId()).get().getPaymentStatus());
        entityManager.flush();
        entityManager.clear();
        System.out.println("Đã lưu trạng thái mới: " + payment.getPaymentStatus());

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            Long orderId = payment.getOrder() != null ? payment.getOrder().getId() : null;
            if (orderId != null && orderRepository.existsById(orderId)) {
                orderRepository.deleteById(orderId);
                System.out.println("Đã xóa đơn hàng có ID: " + orderId);
            } else {
                System.out.println("Không tìm thấy đơn hàng hoặc Order ID null");
            }
        }
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với mã: " + transactionId));
    }

    @Override
    @Transactional
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
        entityManager.flush();
        entityManager.clear();
    }
}