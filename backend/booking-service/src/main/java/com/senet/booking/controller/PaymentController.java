package com.senet.booking.controller;

import com.senet.booking.model.Payment;
import com.senet.booking.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody Payment payment) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(paymentService.processPayment(payment));
    }

    @GetMapping("/booking/{bookingId}")
public ResponseEntity<Payment> getPaymentForBooking(
        @PathVariable String bookingId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-User-Role", required = false) String role) {

    if (userId == null) return ResponseEntity.status(401).build();

    return paymentService.getPaymentForBooking(bookingId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}

    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(paymentService.getMyPayments(userId));
    }
}