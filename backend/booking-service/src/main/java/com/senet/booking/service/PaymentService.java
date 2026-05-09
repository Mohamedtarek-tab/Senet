package com.senet.booking.service;

import com.senet.booking.model.Payment;
import com.senet.booking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    public PaymentService(PaymentRepository paymentRepository, BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @Transactional
    public Payment processPayment(Payment payment) {
        // Simulated payment logic
        payment.setStatus("success");
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentForBooking(String bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> getMyPayments(String userId) {
        List<String> bookingIds = bookingService.getMyBookings(userId)
                .stream().map(b -> b.getId()).collect(Collectors.toList());
        if (bookingIds.isEmpty()) return Collections.emptyList();
        return paymentRepository.findByBookingIdIn(bookingIds);
    }
}
