package com.nooki.service.listing;

import com.nooki.dto.exception.customException.listingException.ListingNotFoundException;
import com.nooki.entity.*;
import com.nooki.repository.ListingRepository;
import com.nooki.service.ListingServices.ListingDomainService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingDomainServiceTests {
    @InjectMocks
    private ListingDomainService listingDomainService;

    @Mock
    private ListingRepository listingRepository;

    @Nested
    @DisplayName("getListingOrThrow")
    class getListing {
        private final UUID listingId = UUID.randomUUID();

        @Test
        @DisplayName("existing listing should return Listing")
        void existingListing_shouldReturnListing() {
            Listing l = new Listing();
            l.setListingId(listingId);
            when(listingRepository.findByIdWithLock(listingId))
                    .thenReturn(Optional.of(l));
            Listing result = listingDomainService.getListingOrThrow(listingId);
            assertThat(l).isEqualTo(result);

            verify(listingRepository, times(1))
                    .findByIdWithLock(listingId);
        }

        @Test
        @DisplayName("Listing not found should throw ListingNotFoundExcception")
        void listingNotFound_shouldThrowListingNotFoundExcception() {
            assertThatThrownBy(() -> listingDomainService.getListingOrThrow(listingId))
                    .isInstanceOf(ListingNotFoundException.class)
                    .hasMessage("entity not exists");
        }

        @Test
        @DisplayName("existing listing should return listing detailed")
        void existsListing_shouldReturnDetailedListing() {
            Listing l = new Listing();
            l.setListingId(listingId);
            when(listingRepository.findByIdWithLock(listingId))
                    .thenReturn(Optional.of(l));
            Listing result = listingDomainService.getListingOrThrow(listingId);
            assertThat(l).isEqualTo(result);
        }

        @Test
        @DisplayName("existing listing should return listing detailed")
        void existsListing_shouldThrowListingNotFoundException() {

            assertThatThrownBy(() -> listingDomainService.getListingOrThrow(listingId))
                    .isInstanceOf(ListingNotFoundException.class)
                    .hasMessage("entity not exists");
        }
    }
}
