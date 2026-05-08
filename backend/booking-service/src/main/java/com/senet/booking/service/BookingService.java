package com.senet.booking.service;

import com.senet.booking.model.Booking;
import com.senet.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getMyBookings(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking createBooking(Booking booking, String userId) {
        if (booking.getId() == null || booking.getId().isEmpty()) {
            booking.setId("BK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        booking.setUserId(userId);
        if (booking.getStatus() == null) {
            booking.setStatus("pending");
        }
        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(String id, String status) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(status);
            return bookingRepository.save(booking);
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public void deleteBooking(String id) {
        bookingRepository.deleteById(id);
    }
}
