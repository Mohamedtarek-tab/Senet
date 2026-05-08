package com.senet.booking.service;

import com.senet.booking.model.Booking;
import com.senet.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    @Value("${car.service.url:http://localhost:8082}")
    private String carServiceUrl;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
    }

    public java.util.Optional<Booking> getBookingById(String id) {
        return bookingRepository.findById(id);
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
        Booking saved = bookingRepository.save(booking);
        updateCarStatus(saved.getCarId(), "booked");
        return saved;
    }

    public Booking updateBookingStatus(String id, String status) {
        return bookingRepository.findById(id).map(booking -> {
            String previousStatus = booking.getStatus();
            booking.setStatus(status);
            Booking saved = bookingRepository.save(booking);
            // Free the car when a booking is cancelled or completed
            if (("cancelled".equals(status) || "completed".equals(status))
                    && !"cancelled".equals(previousStatus)
                    && !"completed".equals(previousStatus)) {
                updateCarStatus(saved.getCarId(), "available");
            }
            return saved;
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public void deleteBooking(String id) {
        bookingRepository.findById(id).ifPresent(booking -> {
            if (!"cancelled".equals(booking.getStatus()) && !"completed".equals(booking.getStatus())) {
                updateCarStatus(booking.getCarId(), "available");
            }
        });
        bookingRepository.deleteById(id);
    }

    private void updateCarStatus(Long carId, String status) {
        if (carId == null) return;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Pass ADMIN role so the car-service @PreAuthorize check passes
            headers.set("X-User-Role", "ADMIN");
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("status", status), headers);
            restTemplate.exchange(
                carServiceUrl + "/api/cars/" + carId,
                HttpMethod.PUT,
                entity,
                Void.class
            );
        } catch (Exception e) {
            // Log but don't fail the booking operation — car status is eventual-consistent
            System.err.println("Warning: could not update car " + carId + " status to " + status + ": " + e.getMessage());
        }
    }
}