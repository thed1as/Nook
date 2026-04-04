package com.library.repository;

import com.library.dto.listing.ListingResponse;
import com.library.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    @Query("SELECT b ALL FROM Booking b WHERE b.user.userId = :userId")
    List<ListingResponse> findAllByUserId(UUID userId);
}
