package com.nooki.repository;

import com.nooki.entity.Booking;
import com.nooki.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.listing.listingId = :listingId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate < :newCheckOut " +
            "AND b.checkOutDate > :newCheckIn")
    boolean isListOccupied(@Param("listingId") UUID listingParam, @Param("newCheckIn") LocalDateTime checkInDate, @Param("newCheckOut") LocalDateTime checkOutDate);


    @EntityGraph(attributePaths = {
            "user",
            "listing",
            "listing.location",
            "listing.user",
            "payment"
    })
    @Query("SELECT b FROM Booking b WHERE b.bookingId IN :ids")
    List<Booking> findUserBookings(@Param("ids") List<UUID> ids);

    @EntityGraph(attributePaths = {
            "user",
            "payment",
            "listing",
            "listing.location",
            "listing.user"
    })
    @Query("SELECT b FROM Booking b WHERE b.listing.listingId = :listingId")
    Page<Booking> findListingBookingsById(UUID listingId, Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.listing.listingId = :listingId
        AND b.status IN ('CONFIRMED', 'CANCELLED')
    """)
    boolean existsActiveBookingsForListing(UUID listingId);


    boolean existsByListing_ListingIdAndUser_UserIdAndStatus(UUID listingListingId, UUID userUserId, Status status);

    @Modifying
    @Query("""
        UPDATE Booking b SET b.status = :cancelledStatus
        WHERE b.status = :pendingStatus AND b.createdAt < :threshold
    """)
    int cancelExpiredBookings(
            @Param("cancelledStatus") Status cancelledStatus,
            @Param("pendingStatus") Status pendingStatus,
            @Param("threshold") LocalDateTime threshold
    );

    @EntityGraph(attributePaths = {
            "user",
            "payment",
            "listing",
            "listing.location"
    })
    @Query("SELECT b FROM Booking b WHERE b.bookingId = :bookingId")
    Optional<Booking> findByDetailedId(@Param("bookingId") UUID bookingId);

    @Query("SELECT b FROM Booking b WHERE b.bookingId = :bookingId")
    @EntityGraph(attributePaths = {
            "user",
            "listing",
            "listing.location",
            "listing.user",
            "payment"
    })
    Optional<Booking> findDetailedForCancelById(@Param("bookingId") UUID bookingId);

    @Query("SELECT b.bookingId FROM Booking b WHERE b.user.userId = :userId")
    Page<UUID> findAllIdsOfUser(Pageable pageable,@Param("userId") UUID userId);
}
