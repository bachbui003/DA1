package com.example.ECM.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
    String createOrder(int total, String orderInfo, String urlReturn);
    int orderReturn(HttpServletRequest request);
}
