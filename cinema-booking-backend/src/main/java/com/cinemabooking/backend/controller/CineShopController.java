package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.request.CineShopOrderRequestDTO;
import com.cinemabooking.backend.payment.model.Payment;
import com.cinemabooking.backend.payment.service.PaymentService;
import com.google.cloud.firestore.Firestore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cine-shop")
@Tag(name = "CineShop", description = "Endpoints for CineShop orders and purchases")
public class CineShopController {

    private static final Logger log = LoggerFactory.getLogger(CineShopController.class);

    @Autowired
    private Firestore firestore;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/orders")
    @Operation(summary = "Create a new CineShop order and generate payment record")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @AuthenticationPrincipal String userId,
            @RequestBody CineShopOrderRequestDTO orderRequest
    ) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }

        try {
            String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("userId", userId);
            orderData.put("type", "CINE_SHOP");
            orderData.put("itemName", orderRequest.getItemName());
            orderData.put("itemImageUrl", orderRequest.getItemImageUrl());
            orderData.put("quantity", orderRequest.getQuantity());
            orderData.put("totalPrice", orderRequest.getTotalPrice());
            orderData.put("paymentMethod", orderRequest.getPaymentMethod().toUpperCase());
            orderData.put("status", "pending");
            orderData.put("createdAt", System.currentTimeMillis());

            // Save order to Firestore
            firestore.collection("cine_shop_orders").document(orderId).set(orderData).get();

            // Create pending payment
            Payment payment = paymentService.createPendingPayment(
                    orderId,
                    userId,
                    orderRequest.getPaymentMethod().toLowerCase(),
                    orderRequest.getTotalPrice()
            );

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderId", orderId);
            responseData.put("paymentId", payment.getPaymentId());
            responseData.put("paymentCode", payment.getPaymentCode());
            responseData.put("totalPrice", payment.getAmount());
            responseData.put("paymentMethod", payment.getProvider());
            responseData.put("createdAt", orderData.get("createdAt"));

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("Order created successfully")
                            .data(responseData)
                            .build()
            );

        } catch (Exception e) {
            log.error("Failed to create CineShop order", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi tạo đơn hàng: " + e.getMessage());
        }
    }
}
