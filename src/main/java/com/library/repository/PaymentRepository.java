package com.library.repository;

import com.library.entity.Payment;
import com.library.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsPaymentByBooking_BookingIdAndStatus(UUID id, PaymentStatus status);

    Optional<Payment> findByStripeId(String stripeId);

    Page<Payment> findByBooking_BookingId(UUID bookingBookingId,
                                          Pageable pageable);

    Page<Payment> findByUser_UserId(UUID userUserId, Pageable pageable);
}
