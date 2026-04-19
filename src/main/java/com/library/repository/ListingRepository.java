package com.library.repository;

import com.library.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    @Query("SELECT l ALL FROM Listing l WHERE l.user.userId = :userId")
    List<Listing> findAllByUserId(UUID userId);

    @Query("SELECT l ALL FROM Listing l WHERE l.user.email = :email")
    List<Listing> findAllByUserEmail(String email);
}
