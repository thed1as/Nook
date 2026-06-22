package com.library.service.ListingServices;

import com.library.dto.exception.customException.listingException.ListingNotFoundException;
import com.library.entity.Listing;
import com.library.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingDomainService {

    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    public Listing getListingOrThrow(UUID listingId) {
        return listingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("entity not exists"));
    }

    @Transactional(readOnly = true)
    public Listing getListingDetailedOrThrow(UUID listingId) {
        return listingRepository.findByDetailedId(listingId)
                .orElseThrow(() -> new ListingNotFoundException("entity not exists"));
    }
}
