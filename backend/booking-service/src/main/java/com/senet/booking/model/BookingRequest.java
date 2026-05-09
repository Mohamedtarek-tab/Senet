package com.senet.booking.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {

    @NotBlank  private String car;
    @NotNull @Positive private Long carId;
    @NotBlank  private String client;
    @NotBlank  private String pickup;
    @NotBlank  private String ret;
    @NotBlank  private String amount;
}