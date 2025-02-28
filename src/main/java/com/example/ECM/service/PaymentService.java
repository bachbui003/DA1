package com.example.ECM.service;

import com.example.ECM.model.Payment;
import com.example.ECM.model.Order;
import com.example.ECM.model.PaymentStatus;

public interface PaymentService {
    Payment createPayment(Order order);
    void updatePaymentStatus(String transactionId, PaymentStatus status, String vnpTransactionId);
    Payment getPaymentByTransactionId(String transactionId);
}
