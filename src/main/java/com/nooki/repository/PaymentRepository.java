package com.nooki.repository;

import com.nooki.entity.Payment;
import com.nooki.enums.PaymentStatus;
import com.nooki.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsPaymentByBooking_BookingIdAndStatus(UUID id, PaymentStatus status);

    Optional<Payment> findByStripeId(String stripeId);

    Page<Payment> findByBooking_BookingId(UUID bookingBookingId,
                                          Pageable pageable);


    @Modifying
    @Query("""
        UPDATE Payment p
            SET p.status = :cancelledStatus
            WHERE p.status = :pendingStatus
              AND p.booking.bookingId IN (
                  SELECT b.bookingId
                  FROM Booking b
                  WHERE b.status = :bookingPendingStatus
                    AND b.createdAt < :threshold
              )
    """)
    int cancelExpiredPayments(
            @Param("cancelledStatus") PaymentStatus cancelledStatus,
            @Param("pendingStatus") PaymentStatus pendingStatus,
            @Param("bookingPendingStatus") Status bookingPendingStatus,
            @Param("threshold") LocalDateTime threshold
    );

    Page<Payment> findByUser_UserId(UUID userUserId, Pageable pageable);
}
