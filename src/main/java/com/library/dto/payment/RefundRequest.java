package com.library.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RefundRequest {
    private UUID paymentId;
    @NotBlank
    private String reason;
}
