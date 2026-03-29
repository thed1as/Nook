package com.library.service;

import com.library.entity.Listing;
import com.library.repository.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;

    public Listing getListingOrThrow(UUID listingId) {
        return listingRepository.findById(listingId).orElseThrow(() -> new EntityNotFoundException("entity not exists"));
    }
}
