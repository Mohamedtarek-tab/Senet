package com.senet.car.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand;

    @NotBlank(message = "Model name is required")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.01", message = "Price per day must be greater than zero")
    private Double pricePerDay;

    @NotBlank(message = "Category is required")
    @Pattern(
        regexp = "^(Sedan|SUV|Sports|Luxury|Electric|Convertible|Truck|Van)$",
        message = "Category must be one of: Sedan, SUV, Sports, Luxury, Electric, Convertible, Truck, Van"
    )
    private String category;

    @Size(max = 50, message = "Engine must not exceed 50 characters")
    private String engine;

    @Pattern(
        regexp = "^(Automatic|Manual)$",
        message = "Transmission must be Automatic or Manual"
    )
    private String transmission;

    @Column(length = 1000)
    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    private String imageUrl;

    @Pattern(
        regexp = "^(available|booked)$",
        message = "Status must be 'available' or 'booked'"
    )
    private String status;

    @Column(length = 2000)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
}