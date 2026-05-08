package com.senet.booking.repository;

import com.senet.booking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(String bookingId);
    List<Payment> findByBookingIdIn(List<String> bookingIds);
}
