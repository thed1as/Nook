package com.library.service;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.dto.location.LocationRequest;
import com.library.entity.Listing;
import com.library.entity.ListingImage;
import com.library.entity.Location;
import com.library.entity.User;
import com.library.mapper.ListingMapper;
import com.library.repository.BookingRepository;
import com.library.repository.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTests {
    @InjectMocks
    private ListingService listingService;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private LocationService locationService;

    @Mock
    private MinioService minioService;

    @Mock
    private ListingMapper listingMapper;

    @Test
    void createListing_ValidRequest_ReturnsListingResponse() {
        String email = "test@gmail.com";
        ListingRequest listingRequest = new ListingRequest();
        listingRequest.setListingTitle("Luxury Loft");
        listingRequest.setDescription("Nice view");
        listingRequest.setPricePerNight(BigDecimal.valueOf(1000));

        LocationRequest locReq = new LocationRequest();
        listingRequest.setLocationRequest(locReq);

        User user = spy(new User());
        Location location = spy(new Location());

        ListingResponse expectedResponse = new ListingResponse();
        expectedResponse.setListingTitle("Luxury Loft");

        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(userService.getUserByEmail(email)).thenReturn(user);
        when(locationService.createLocationOrGet(any())).thenReturn(location);
        when(listingMapper.toListingResponse(any())).thenReturn(expectedResponse);

        ListingResponse result = listingService.createListing(listingRequest);

        assertEquals(expectedResponse, result);

        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(listingCaptor.capture());

        Listing savedListing = listingCaptor.getValue();
        assertEquals("Luxury Loft", savedListing.getTitle());
        assertEquals(BigDecimal.valueOf(1000), savedListing.getPricePerNight());

        verify(user).addListing(any(Listing.class));
        verify(location).addListing(any(Listing.class));
    }

    @Test
    void updateListing_validRequest_ReturnsListingResponse() {
        UUID listingId = UUID.randomUUID();
        String email = "test@gmail.com";

        UpdateListingRequest updateListingRequest = new UpdateListingRequest();
        updateListingRequest.setListingTitle("New title");

        User user = new User();
        user.setEmail(email);

        Location location = new Location();
        location.setCountry("city");
        location.setCity("city");
        location.setAddress("city");

        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setUser(user);
        listing.setTitle("Old title");
        listing.setLocation(location);

        LocationRequest newLoc = new LocationRequest();
        newLoc.setCountry("city");
        newLoc.setCity("city");
        newLoc.setAddress("city");
        updateListingRequest.setLocationRequest(newLoc);

        ListingResponse expectedResponse = new ListingResponse();
        expectedResponse.setListingTitle("New title");

        when(listingRepository.findByIdWithLock(listingId))
                .thenReturn(Optional.of(listing));
        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(listingMapper.toListingResponse(any(Listing.class))).thenReturn(expectedResponse);

        ListingResponse result = listingService.updateListing(updateListingRequest, listingId);

        assertEquals("New title", result.getListingTitle());

        verify(listingMapper).updateListing(updateListingRequest, listing);
    }

    @Test
    void updateListing_notOnwer_ThrowsIllegalStateException() {
        UpdateListingRequest updateListingRequest = new UpdateListingRequest();
        UUID listingId = UUID.randomUUID();
        User user = new User();
        user.setEmail("owner@gmail.com");
        Listing listing = new Listing();
        listing.setUser(user);
        String email = "notOwner@gmail.com";

        when(listingRepository.findByIdWithLock(listingId))
                .thenReturn(Optional.of(listing));
        when(userService.getCurrentUserEmail()).thenReturn(email);

        assertThatThrownBy(() -> listingService.updateListing(updateListingRequest, listingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not your listing");

        verify(listingMapper, never()).updateListing(any(), any());
    }

    @Test
    void updateListing_validRequestCreatingNewLoc_ReturnsListingResponse() {
        UUID listingId = UUID.randomUUID();
        String email = "test@gmail.com";

        UpdateListingRequest updateListingRequest = new UpdateListingRequest();
        updateListingRequest.setListingTitle("New title");

        User user = new User();
        user.setEmail(email);

        Location location = new Location();
        location.setCountry("old country");
        location.setCity("old city");
        location.setAddress("old address");

        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setUser(user);
        listing.setTitle("Old title");
        listing.setLocation(location);

        LocationRequest newLoc = new LocationRequest();
        newLoc.setCountry("new country");
        newLoc.setCity("new city");
        newLoc.setAddress("new address");
        updateListingRequest.setLocationRequest(newLoc);

        ListingResponse expectedResponse = new ListingResponse();
        expectedResponse.setListingTitle("New title");

        when(listingRepository.findByIdWithLock(listingId))
                .thenReturn(Optional.of(listing));
        when(userService.getCurrentUserEmail()).thenReturn(email);
        when(listingMapper.toListingResponse(any(Listing.class))).thenReturn(expectedResponse);

        ListingResponse result = listingService.updateListing(updateListingRequest, listingId);

        assertEquals("New title", result.getListingTitle());

        verify(listingMapper).updateListing(updateListingRequest, listing);
    }

    @Test
    void getListingById_validRequest_ReturnsListingResponse() {
        UUID listingId = UUID.randomUUID();
        Listing listing = new Listing();
        ListingResponse expectedResponse = new ListingResponse();

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.of(listing));
        when(listingMapper.toListingResponse(listing))
                .thenReturn(expectedResponse);

        ListingResponse result = listingService.getListingById(listingId);

        assertEquals(expectedResponse, result);

        verify(listingRepository).findById(listingId);

    }

    @Test
    void getListingById_failRequest_ThrowsEntityNotFoundException() {
        UUID listingId = UUID.randomUUID();

        when(listingRepository.findById(listingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingById(listingId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(listingRepository).findById(listingId);
    }

    @Test
    void getUsersListings_validRequest_ReturnsListOfListingResponse() {
        String email = "test@gmail.com";
        Listing listing = new Listing();
        ListingResponse expectedResponse = new ListingResponse();
        List<Listing> l1 = new ArrayList<>();
        l1.add(listing);
        List<ListingResponse> l2 = new ArrayList<>();
        l2.add(expectedResponse);

        when(listingRepository.findAllByUserEmail(email)).thenReturn(l1);
        when(listingMapper.toListingResponse(any(Listing.class))).thenReturn(new ListingResponse());

        List<ListingResponse> result = listingService.getUsersListings(email);
        assertEquals(l2, result);

        verify(listingRepository).findAllByUserEmail(email);
    }

    @Test
    void getUsersListings_failRequest_ReturnsEmptyList() {
        String email = "test@gmail.com";

        when(listingRepository.findAllByUserEmail(email))
                .thenReturn(Collections.emptyList());

        List<ListingResponse> result = listingService.getUsersListings(email);

        assertTrue(result.isEmpty());

        verify(listingRepository).findAllByUserEmail(email);
    }

    @Test
    void getAll_validRequest_ReturnsPageOfListingResponse() {
        Pageable pageable = PageRequest.of(0, 2);

        Listing l1 = new Listing();
        Listing l2 = new Listing();

        ListingResponse lr1 = new ListingResponse();
        ListingResponse lr2 = new  ListingResponse();

        List<Listing> listings = List.of(l1, l2);
        Page<Listing> listingPage = new PageImpl<>(listings);

        when(listingRepository.findAll(pageable)).thenReturn(listingPage);
        when(listingMapper.toListingResponse(l1)).thenReturn(lr1);
        when(listingMapper.toListingResponse(l2)).thenReturn(lr2);

        Page<ListingResponse> result = listingService.getAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(lr1, result.getContent().get(0));
        assertEquals(lr2, result.getContent().get(1));

        verify(listingRepository).findAll(pageable);
        verify(listingMapper).toListingResponse(l1);
        verify(listingMapper).toListingResponse(l2);
    }

    @Test
    void deleteListingById_validRequest_DeleteListingResponse() {
        UUID listingId = UUID.randomUUID();

        ListingImage image = new ListingImage();
        image.setFileName("test");

        List<ListingImage> images = new ArrayList<>();
        images.add(image);

        Listing listing = new Listing();
        listing.setListingImages(images);

        when(bookingRepository.existsActiveBookingsForListing(listingId)).thenReturn(false);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        listingService.deleteListingById(listingId);

        verify(minioService).deleteFile("test");
        verify(listingRepository, times(1)).delete(listing);
    }

    @Test
    void deleteListingById_existsActiveBookings_ThrowsIllegalStateException() {
        UUID listingId = UUID.randomUUID();

        when(bookingRepository.existsActiveBookingsForListing(listingId)).thenReturn(true);

        assertThatThrownBy(() -> listingService.deleteListingById(listingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete booking with active future bookings");

        verify(bookingRepository, never()).delete(any());
    }

    @Test
    void getListingOrThrow_validRequest_ReturnsListing() {
        UUID listingId = UUID.randomUUID();

        Listing expectedListing = new Listing();
        expectedListing.setListingId(listingId);

        when(listingRepository.findByIdWithLock(listingId))
                .thenReturn(Optional.of(expectedListing));

        Listing result = listingService.getListingOrThrow(listingId);

        assertSame(expectedListing, result);

        verify(listingRepository).findByIdWithLock(listingId);
    }

    @Test
    void getListingOrThrow_failRequest_ThrowsEntityNotFoundException() {
        UUID listingId = UUID.randomUUID();

        when(listingRepository.findByIdWithLock(listingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingOrThrow(listingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("entity not exists");

        verify(listingRepository).findByIdWithLock(listingId);
    }
}
