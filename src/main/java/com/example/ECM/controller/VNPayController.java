package com.example.ECM.controller;

import com.example.ECM.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/payments")
public class VNPayController {

    private final VNPayService vnPayService;

    // ✅ Sử dụng Constructor Injection thay vì @Autowired trên field
    public VNPayController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    // ✅ 1. Tạo URL Thanh Toán VNPay
    @PostMapping("/submitOrder")
    public ResponseEntity<Map<String, String>> submitOrder(
            @RequestParam("amount") int orderTotal,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);

        // ✅ Trả về URL để frontend điều hướng
        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", vnpayUrl);

        return ResponseEntity.ok(response);
    }

    // ✅ 2. Xử lý phản hồi từ VNPay
    @GetMapping("/vnpay-payment")
    public ResponseEntity<Map<String, Object>> processVNPayPayment(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", paymentStatus == 1 ? "SUCCESS" : "FAILED");
        response.put("orderId", request.getParameter("vnp_OrderInfo"));
        response.put("totalPrice", Long.parseLong(request.getParameter("vnp_Amount")) / 100); // VNPay trả về số tiền nhân 100
        response.put("paymentTime", request.getParameter("vnp_PayDate"));
        response.put("transactionId", request.getParameter("vnp_TransactionNo"));

        return ResponseEntity.ok(response);
    }
}
