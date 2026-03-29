package com.library.repository;

import com.library.entity.Booking;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.listing.id = :listingId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate < :newCheckOut " +
            "AND b.checkOutDate > :newCheckIn")
    boolean isListOccupied(@Param("listingId") UUID listingParam, @Param("newCheckIn") LocalDateTime checkInDate, @Param("newCheckOut") LocalDateTime checkOutDate);
}
