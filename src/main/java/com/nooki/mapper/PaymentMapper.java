package com.nooki.mapper;

import com.nooki.dto.payment.PaymentResponse;
import com.nooki.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "bookingId", source = "booking.bookingId")
    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "paymentMethod", source = "method")
    @Mapping(target = "createdAt", source = "createdAt")
    PaymentResponse toPaymentResponse(Payment payment);
}
