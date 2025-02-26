package com.example.ECM.controller;

import com.example.ECM.dto.PaymentDTO;
import com.example.ECM.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // API tạo thanh toán
    @PostMapping("/create")
    public ResponseEntity<PaymentDTO> createPayment(@RequestParam Long orderId, @RequestParam int amount) {
        PaymentDTO payment = paymentService.createPayment(orderId, amount);
        return ResponseEntity.ok(payment);
    }

    // API cập nhật trạng thái thanh toán
    @PutMapping("/update-status")
    public ResponseEntity<Map<String, String>> updatePaymentStatus(@RequestParam String paymentCode, @RequestParam String status) {
        paymentService.updatePaymentStatus(paymentCode, status);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cập nhật trạng thái thành công");
        return ResponseEntity.ok(response);
    }

    // API lấy thông tin thanh toán
    @GetMapping("/{paymentCode}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable String paymentCode) {
        PaymentDTO payment = paymentService.getPaymentByCode(paymentCode);
        return ResponseEntity.ok(payment);
    }
}
