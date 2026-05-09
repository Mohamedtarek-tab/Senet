package com.senet.booking.controller;

import com.senet.booking.model.Payment;
import com.senet.booking.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPayment(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestBody Payment payment) {
    if (userId == null) return ResponseEntity.status(401).build();
    return ResponseEntity.ok(paymentService.processPaymentAndConfirm(payment));
}

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<?> getMyPayments(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(paymentService.getMyPayments(userId));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Payment> getPaymentForBooking(
            @PathVariable String bookingId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        return paymentService.getPaymentForBooking(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
}