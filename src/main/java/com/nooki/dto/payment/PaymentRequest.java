package com.nooki.dto.payment;

import com.nooki.enums.PaymentMethod;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    @NotBlank @Size(min = 3, max = 3)
    String currency;
}