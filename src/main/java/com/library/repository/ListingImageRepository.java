package com.library.repository;

import com.library.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> { }
