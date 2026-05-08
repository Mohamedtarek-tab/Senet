package com.senet.booking.controller;

import com.senet.booking.model.Booking;
import com.senet.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<?> getAllBookings(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                                 @RequestBody Booking booking) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bookingService.createBooking(booking, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@RequestHeader(value = "X-User-Role", required = false) String role,
                                           @PathVariable String id) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        try { bookingService.deleteBooking(id); return ResponseEntity.noContent().build(); }
        catch (Exception e) { return ResponseEntity.notFound().build(); }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String id) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Booking booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            // Allow if the caller owns the booking, or is an admin
            if (!"ADMIN".equals(role) && !userId.equals(booking.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only cancel your own bookings");
            }
            if ("completed".equals(booking.getStatus())) {
                return ResponseEntity.badRequest().body("Cannot cancel a completed booking");
            }
            return ResponseEntity.ok(bookingService.updateBookingStatus(id, "cancelled"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                 @PathVariable String id,
                                                 @RequestBody Map<String, String> body) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            return ResponseEntity.ok(bookingService.updateBookingStatus(id, body.get("status")));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}