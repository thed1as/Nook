package com.nooki.repository;

import java.util.Optional;
import java.util.UUID;

import com.nooki.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    Optional<ListingImage> findByListingImageIdAndListing_ListingId(
            Long imageId,
            UUID listingId
    );}
