package com.library.repository;

import com.library.entity.Booking;
import com.library.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.listing.id = :listingId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate < :newCheckOut " +
            "AND b.checkOutDate > :newCheckIn")
    boolean isListOccupied(@Param("listingId") UUID listingParam, @Param("newCheckIn") LocalDateTime checkInDate, @Param("newCheckOut") LocalDateTime checkOutDate);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId AND b.listing.listingId = :listingId")
    Optional<Booking> findByListingIdAndUserId(UUID listingId, UUID userId);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId")
    List<Booking> findUserBookingsById(UUID userId);

    UUID listing(Listing listing);
}
