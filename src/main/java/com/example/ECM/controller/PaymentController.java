package com.example.ECM.controller;

import com.example.ECM.model.Order;
import com.example.ECM.model.Payment;
import com.example.ECM.service.OrderService;
import com.example.ECM.service.PaymentService;
import com.example.ECM.service.Impl.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayService vnPayService;

    @PostMapping("/create/{orderId}")
    public String createPayment(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        Payment payment = paymentService.createPayment(order);
        String returnUrl = "http://localhost:8080/api/payment/vnpay-return";
        return vnPayService.createOrder(order.getTotalPrice().intValue(), "Thanh toán đơn hàng", returnUrl);
    }

    @GetMapping("/vnpay-return")
    public String paymentReturn(@RequestParam Map<String, String> params) {
        int result = vnPayService.orderReturn(null);
        if (result == 1) {
            paymentService.updatePaymentStatus(params.get("vnp_TxnRef"), "PAID");
            return "Thanh toán thành công";
        } else {
            return "Thanh toán thất bại";
        }
    }
}
