package com.senet.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @Pattern(regexp = "^BK-[A-Z0-9]+$", message = "Booking ID must match format BK-XXXXXX")
    private String id;

    @NotBlank(message = "Car name is required")
    private String car;

    @NotNull(message = "Car ID is required")
    @Positive(message = "Car ID must be a positive number")
    private Long carId;

    @NotBlank(message = "Client name is required")
    @Size(max = 100, message = "Client name must not exceed 100 characters")
    private String client;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Pickup date is required")
    private String pickup;

    @NotBlank(message = "Return date is required")
    private String ret;

    @NotBlank(message = "Amount is required")
    private String amount;

    @Pattern(
        regexp = "^(pending|confirmed|completed|cancelled)$",
        message = "Status must be one of: pending, confirmed, completed, cancelled"
    )
    private String status;
}