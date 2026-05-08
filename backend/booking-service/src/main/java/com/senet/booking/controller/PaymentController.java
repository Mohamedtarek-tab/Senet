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
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.processPayment(payment));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Payment> getPaymentForBooking(@PathVariable String bookingId) {
        return paymentService.getPaymentForBooking(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Payment>> getMyPayments(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(paymentService.getMyPayments(userId));
    }
}
