package com.senet.booking.service;

import com.senet.booking.model.Booking;
import com.senet.booking.repository.BookingRepository;
import com.senet.booking.repository.PaymentRepository;
import com.senet.booking.strategy.StatusTransitionStrategy;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final StatusTransitionStrategy statusStrategy;

    @Value("${car.service.url:http://localhost:8082}")
    private String carServiceUrl;

    public BookingService(BookingRepository bookingRepository,
            StatusTransitionStrategy statusStrategy, 
            RestTemplate restTemplate) {
        this.bookingRepository = bookingRepository;
        this.statusStrategy = statusStrategy;
        this.restTemplate = restTemplate;
    }
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(String id) {
        return bookingRepository.findById(id);
    }
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    @Transactional(readOnly = true)
    public List<Booking> getMyBookings(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
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

    @Transactional
    public Booking updateBookingStatus(String id, String requestedStatus) {
        return bookingRepository.findById(id).map(booking -> {
            String previousStatus = booking.getStatus();

            // Strategy pattern — validates the transition is legal before applying it
            String newStatus = statusStrategy.transition(previousStatus, requestedStatus);

            booking.setStatus(newStatus);
            Booking saved = bookingRepository.save(booking);

            // Free the car when a booking is cancelled or completed
            if (("cancelled".equals(newStatus) || "completed".equals(newStatus))
                    && !"cancelled".equals(previousStatus)
                    && !"completed".equals(previousStatus)) {
                updateCarStatus(saved.getCarId(), "available");
            }
            return saved;
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    @Transactional
    public void deleteBooking(String id) {
        bookingRepository.findById(id).ifPresent(booking -> {
            if (!"cancelled".equals(booking.getStatus()) && !"completed".equals(booking.getStatus())) {
                updateCarStatus(booking.getCarId(), "available");
            }
        });
        bookingRepository.deleteById(id);
    }

    private void updateCarStatus(Long carId, String status) {
        if (carId == null)
            return;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Role", "ADMIN");
            headers.set("X-Internal-Call", "true");
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("status", status), headers);
            restTemplate.exchange(
                    carServiceUrl + "/api/cars/" + carId,
                    HttpMethod.PUT,
                    entity,
                    Void.class);
        } catch (Exception e) {
            System.err
                    .println("Warning: could not update car " + carId + " status to " + status + ": " + e.getMessage());
        }
    }
}