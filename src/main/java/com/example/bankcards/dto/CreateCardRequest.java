package com.example.bankcards.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateCardRequest {
    @NotBlank
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank
    private String cardHolder;

    @NotNull
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    @NotNull
    private Long userId;
}