package com.nooki.repository;

import com.nooki.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Review> findTop3ByListing_ListingIdOrderByCreatedAtDesc(UUID listingListingId);

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findAllByListing_ListingIdOrderByCreatedAtDesc(UUID listingListingId,
                                                                Pageable pageable);

    Long countAllByListing_ListingIdAndUser_UserId(UUID listingId, UUID userId);

    @EntityGraph(attributePaths = {"user", "listing"})
    @Query("SELECT r FROM Review r WHERE r.reviewId = :reviewId")
    Optional<Review> findByDetailedReviewId(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.reviewId = :reviewId")
    void deleteByDetailedId(@Param("reviewId") Long id);
}
