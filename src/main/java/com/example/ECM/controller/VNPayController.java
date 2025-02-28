package com.example.ECM.controller;

import com.example.ECM.service.Impl.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/payments")
public class VNPayController {

    @Autowired
    private VNPayService vnPayService;

    // ✅ 1. API Tạo URL Thanh Toán VNPay (Test bằng Postman)
    @PostMapping("/submitOrder")
    public ResponseEntity<Map<String, String>> submitOrder(
            @RequestParam("amount") int orderTotal,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(orderTotal, orderInfo, baseUrl);

        // ✅ Trả về URL để test trên Postman
        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", vnpayUrl);

        return ResponseEntity.ok(response);
    }

    // ✅ 2. API Nhận Callback từ VNPay
    @GetMapping("/vnpay-payment")
    public ResponseEntity<Map<String, Object>> processVNPayPayment(HttpServletRequest request) {
        System.out.println("🔄 VNPay Callback Received!");

        Map<String, String[]> paramMap = request.getParameterMap();
        paramMap.forEach((key, value) -> System.out.println(key + ": " + Arrays.toString(value)));

        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalAmount = request.getParameter("vnp_Amount");
        String paymentTime = request.getParameter("vnp_PayDate");

        int paymentStatus = vnPayService.orderReturn(request);
        String status = (paymentStatus == 1) ? "SUCCESS" : "FAILED";

        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("orderId", orderInfo);
        response.put("totalPrice", Long.parseLong(totalAmount) / 100);
        response.put("paymentTime", paymentTime);
        response.put("transactionId", transactionId);

        return ResponseEntity.ok(response);
    }


}
