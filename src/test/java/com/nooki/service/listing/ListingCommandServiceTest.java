package com.nooki.service.listing;

import com.nooki.dto.exception.customException.listingException.ListingCancelException;
import com.nooki.dto.exception.customException.listingException.ListingException;
import com.nooki.dto.exception.customException.listingException.ListingIllegalStateException;
import com.nooki.dto.listing.ListingRequest;
import com.nooki.dto.listing.ListingResponse;
import com.nooki.dto.listing.UpdateListingRequest;
import com.nooki.dto.location.LocationRequest;
import com.nooki.dto.location.LocationResponse;
import com.nooki.entity.*;
import com.nooki.mapper.ListingMapper;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.ListingServices.ListingCommandService;
import com.nooki.service.ListingServices.ListingDomainService;
import com.nooki.service.ListingServices.ListingImageService;
import com.nooki.service.LocationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import com.nooki.repository.BookingRepository;
import com.nooki.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingCommandServiceTest {
    @InjectMocks
    private ListingCommandService listingCommandService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingMapper listingMapper;

    @Mock
    private ListingDomainService listingDomainService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ListingImageService listingImageService;

    @Nested
    @DisplayName("creating listing")
    class createListing {
        private final UUID userId = UUID.randomUUID();

        @Test
        @DisplayName("valid request should create listing and return listingResponse")
        void validRequest_shouldCreateListing() {
            ListingRequest listingRequest = new ListingRequest();
            listingRequest.setListingTitle("test title");
            listingRequest.setDescription("test description");
            listingRequest.setPricePerNight(new BigDecimal("5.0"));
            listingRequest.setCurrency("USD");
            listingRequest.setLocationRequest(new LocationRequest());

            User user = new User();
            user.setUserId(userId);
            Location location = new Location();

            ListingResponse expectedResponse = new ListingResponse();
            expectedResponse.setCurrency("USD");
            expectedResponse.setPricePerNight(new BigDecimal("5.0"));
            expectedResponse.setListingDescription("Test description");
            expectedResponse.setListingTitle("test title");

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(userRepository.getReferenceById(userId)).thenReturn(user);
            when(locationService.createLocationOrGet(any(LocationRequest.class))).thenReturn(location);
            when(listingMapper.toListingResponse(any(Listing.class)))
                    .thenReturn(expectedResponse);

            ListingResponse result = listingCommandService.createListing(listingRequest);

            assertThat(expectedResponse).isEqualTo(result);

            ArgumentCaptor<Listing> listingArgumentCaptor = ArgumentCaptor.forClass(Listing.class);
            verify(listingRepository).save(listingArgumentCaptor.capture());

            Listing capturedListing = listingArgumentCaptor.getValue();

            assertThat(capturedListing.getUser()).isEqualTo(user);
            assertThat(capturedListing.getTitle())
                    .isEqualTo("test title");

            assertThat(capturedListing.getDescription())
                    .isEqualTo("test description");

            assertThat(capturedListing.getPricePerNight())
                    .isEqualByComparingTo("5.0");

            assertThat(capturedListing.getCurrency())
                    .isEqualTo("USD");

            assertThat(capturedListing.getUser())
                    .isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("update listing")
    class updateListing {
        private final UUID userId = UUID.randomUUID();
        private final UUID listingId = UUID.randomUUID();

        @Test
        @DisplayName("valid request")
        void validRequest_shouldUpdateListingAndReturnListingResponse() {
            LocationRequest lr = new LocationRequest();
            lr.setCountry("Somewhere [test]");
            lr.setCity("Somewhere [test]");
            lr.setAddress("Somewhere [test]");

            UpdateListingRequest updateListingRequest = new UpdateListingRequest();
            updateListingRequest.setListingTitle("test title");
            updateListingRequest.setCurrency("USD");
            updateListingRequest.setLocationRequest(lr);

            UUID userId = UUID.randomUUID();
            User host = new User();
            host.setUserId(userId);

            Location old = new Location();
            old.setCountry("oldCountry");
            old.setCity("oldCity");
            old.setAddress("oldAddress");

            Location currentExpected = new Location();
            currentExpected.setCountry("Somewhere [test]");
            currentExpected.setCity("Somewhere [test]");
            currentExpected.setAddress("Somewhere [test]");

            Listing l = new Listing();

            l.setListingId(listingId);
            l.setLocation(old);
            l.setUser(host);

            ListingResponse expectedResponse = new ListingResponse();
            LocationResponse locationResponse = new LocationResponse();
            locationResponse.setAddress("Somewhere [test]");
            locationResponse.setCountry("Somewhere [test]");
            locationResponse.setCity("Somewhere [test]");
            expectedResponse.setLocation(locationResponse);
            expectedResponse.setCurrency("USD");
            expectedResponse.setListingTitle("test title");
            expectedResponse.setListingId(listingId);

            when(listingDomainService.getListingOrThrow(listingId)).thenReturn(l);
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(listingDomainService.getListingDetailedOrThrow(listingId)).thenReturn(l);
            when(locationService.createLocationOrGet(lr)).thenReturn(currentExpected);
            when(listingMapper.toListingResponse(any(Listing.class))).thenReturn(expectedResponse);

            ListingResponse result = listingCommandService.updateListing(updateListingRequest, listingId);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.getListingId()).isEqualTo(listingId);
            assertThat(result.getLocation().getCity()).isEqualTo(currentExpected.getCity());
            assertThat(result.getCurrency()).isEqualTo("USD");
        }


        @Test
        @DisplayName("not owner tried to update listing should throw ListingIllegalStateException")
        void notOwnerTriedToUpdateListing_shouldThrowListingIllegalStateException() {
            UpdateListingRequest ulr = new UpdateListingRequest();
            User user = new User();
            user.setUserId(userId);

            User host = new User();
            host.setUserId(UUID.randomUUID());

            Listing l = new Listing();
            l.setListingId(listingId);
            l.setUser(host);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(listingDomainService.getListingOrThrow(listingId)).thenReturn(l);
            when(listingDomainService.getListingDetailedOrThrow(listingId)).thenReturn(l);

            assertThatThrownBy(() -> listingCommandService.updateListing(ulr, listingId))
                    .isInstanceOf(ListingIllegalStateException.class)
                    .hasMessage("Not your listing");

        }
    }

    @Nested
    @DisplayName("delete listing")
    class deleteListing {
        private final UUID userId = UUID.randomUUID();
        private final UUID listingId = UUID.randomUUID();

        @Test
        @DisplayName("valid requeset should delete listing by id")
        void validRequest_shouldDeleteListingById() {
            User host = new User();
            host.setUserId(userId);

            Listing listing = new Listing();
            listing.setListingId(listingId);
            listing.setUser(host);

            when(userService.getCurrentUserId()).thenReturn(userId);
            when(bookingRepository.existsActiveBookingsForListing(listingId)).thenReturn(false);
            when(listingRepository.findById(listingId))
                    .thenReturn(Optional.of(listing));

            listingCommandService.deleteListingById(listingId);

            ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
            verify(listingRepository, times(1)).deleteById(captor.capture());

            UUID deletedListingId = captor.getValue();
            assertThat(deletedListingId).isEqualTo(listingId);
        }

        @Test
        @DisplayName("host tried to delete listing with active bookings should throw ListingCancelException")
        void hostTriedToDeleteWithActiveBookings_shouldThrowListingCancelException() {
            when(userService.getCurrentUserId()).thenReturn(userId);
            when(bookingRepository.existsActiveBookingsForListing(listingId)).thenReturn(true);

            assertThatThrownBy(() -> listingCommandService.deleteListingById(listingId))
                    .isInstanceOf(ListingCancelException.class)
                    .hasMessage("Cannot delete booking with active future bookings");
        }

        @Test
        @DisplayName("not host tried to delete listing should throw ListingException")
        void notHostTriedToDeleteListing_shouldThrowListingException() {
            User host = new User();
            host.setUserId(userId);

            Listing listing = new Listing();
            listing.setUser(host);

            when(userService.getCurrentUserId()).thenReturn(UUID.randomUUID());
            when(bookingRepository.existsActiveBookingsForListing(listingId)).thenReturn(false);
            when(listingRepository.findById(listingId))
                    .thenReturn(Optional.of(listing));

            assertThatThrownBy(() -> listingCommandService.deleteListingById(listingId))
                    .isInstanceOf(ListingException.class)
                    .hasMessage("Not your listing");
        }
    }
}
