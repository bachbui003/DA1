package com.example.ECM.controller;

import com.example.ECM.service.Impl.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/payments")
public class VNPayController {

    private final VNPayService vnPayService;

    public VNPayController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    // ✅ Tạo URL Thanh Toán VNPay từ JSON
    @PostMapping("/submitOrder")
    public ResponseEntity<Map<String, String>> submitOrder(@RequestBody Map<String, Object> requestBody) {
        int orderTotal = (Integer) requestBody.get("amount");
        String orderInfo = (String) requestBody.get("orderInfo");
        String baseUrl = "http://localhost:8080"; // URL Backend xử lý

        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", vnpayUrl);
        return ResponseEntity.ok(response);
    }

    // ✅ Xử lý phản hồi từ VNPay
    @PostMapping("/vnpay-payment")
    public ResponseEntity<Map<String, Object>> processVNPayPayment(@RequestBody Map<String, Object> requestBody) {
        int paymentStatus = vnPayService.orderReturn(requestBody);

        Map<String, Object> response = new HashMap<>();
        response.put("status", paymentStatus == 1 ? "SUCCESS" : "FAILED");
        response.put("orderId", requestBody.get("vnp_OrderInfo"));
        response.put("totalPrice", ((Number) requestBody.get("vnp_Amount")).longValue() / 100);
        response.put("paymentTime", requestBody.get("vnp_PayDate"));
        response.put("transactionId", requestBody.get("vnp_TransactionNo"));

        return ResponseEntity.ok(response);
    }
}
