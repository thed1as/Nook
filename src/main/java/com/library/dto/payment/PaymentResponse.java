package com.library.dto.payment;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private UUID bookingId;
}
