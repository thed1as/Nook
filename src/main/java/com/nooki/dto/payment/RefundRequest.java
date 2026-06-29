package com.nooki.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RefundRequest {
    private UUID paymentId;
    @NotBlank
    private String reason;
}
