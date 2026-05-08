package com.senet.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    private String cardNumber;
    private String expiry;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;

    @Pattern(
        regexp = "^(success|failed)$",
        message = "Status must be 'success' or 'failed'"
    )
    private String status;
}