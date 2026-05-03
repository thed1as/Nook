package com.library.controller;

import com.library.config.SecurityConfig;
import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.enums.Status;
import com.library.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class})
@DisplayName("BookingController testing")
public class BookingControllerTests extends AbstractControllerTest {
    @MockitoBean
    private BookingService bookingService;

    @Nested
    @DisplayName("Booking (POST /booking)")
    class CreateBooking {
        private final String URL = "/api/booking";

        @Test
        @DisplayName("Return 201, if data is valid")
        @WithMockUser(roles = "USER")
        void validRequest_ShouldCreateBooking() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(UUID.randomUUID());
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(1));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(5));

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingRequest))
            ).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Return 400, if ID field is empty (valid)")
        @WithMockUser(roles = "USER")
        void incorrectListingIdRequest_ShouldReturn400() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(null);
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(1));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(5));

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Return 400, if date field in past (valid)")
        @WithMockUser(roles = "USER")
        void incorrectListingDateRequest_ShouldReturn400() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(UUID.randomUUID());
            bookingRequest.setCheckInDate(LocalDateTime.now().minusDays(1));
            bookingRequest.setCheckInDate(LocalDateTime.now().minusDays(5));

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Return 409, if checkOutDate > checkInDate")
        @WithMockUser(roles = "USER")
        void incorrectListingDateRequest_ShouldReturn409() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(UUID.randomUUID());
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(10));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(5));

            when(bookingService.createBooking(any(BookingRequest.class)))
                    .thenThrow(new IllegalStateException("Invalid date range"));

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingRequest))
            ).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Return 401, if anonymous user trying to book")
        void anonymousUser_ShouldReturn401() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(UUID.randomUUID());
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(1));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(4));

            mockMvc.perform(post(URL).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingRequest))
            ).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Booking (GET /booking/id")
    class GetBookingById {
        private final UUID listingId = UUID.randomUUID();
        private final UUID bookingId = UUID.randomUUID();
        private final String URL = "/api/booking/" + bookingId;

        @Test
        @DisplayName("Get booking by id valid requests")
        @WithMockUser(roles = "USER")
        void validRequest_ShouldReturn200() throws Exception {
            BookingResponse br = new BookingResponse();
            br.setListingId(listingId);

            when(bookingService.getBookingById(bookingId)).thenReturn(br);

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.listingId").value(listingId.toString()));
        }

        @Test
        @DisplayName("Get booking by id (non-existent listing)")
        @WithMockUser(roles = "USER")
        void nonExistentListing_ShouldReturn409() throws Exception {
            when(bookingService.getBookingById(bookingId))
                    .thenThrow(new EntityNotFoundException
                    ("Booking not found with id: " + bookingId));

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get my bookings (GET /bookings/my)")
    class GetMyBookings {
        private final String URL = "/api/bookings/my";

        @Test
        @DisplayName("Get my bookings (success)")
        @WithMockUser(roles = "USER")
        void getMyBookings_shouldReturn200() throws Exception {
            BookingResponse br = new BookingResponse();
            List<BookingResponse> brl = new ArrayList<>();

            brl.add(br);

            when(bookingService.getMyBookings()).thenReturn(brl);

            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Get my bookings (GET /bookings/{id}")
    class GetBookings {
        private final UUID listingId = UUID.randomUUID();
        private final String URL = "/api/booking/" + listingId;

        @Test
        @DisplayName("Get listing bookings (success)")
        @WithMockUser(roles = "USER")
        void getListingBookings_shouldReturn200() throws Exception {
            BookingResponse br = new BookingResponse();
            List<BookingResponse> brl = new ArrayList<>();
            brl.add(br);

            when(bookingService.getListingBookings(listingId)).thenReturn(brl);
            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Cancelling booking (DELETEMAPPING /bookings/{id})")
    class CancellBooking {
        private final UUID bookingId = UUID.randomUUID();
        private final String URL = "/api/bookings/" + bookingId;

        @Test
        @DisplayName("Succesfully canceled book")
        @WithMockUser(roles = "USER")
        void validRequest_ShouldReturn200() throws Exception {
            BookingResponse br = new BookingResponse();
            br.setStatus(Status.CANCELLED);
            when(bookingService.cancelBooking(bookingId)).thenReturn(br);

            mockMvc.perform(delete(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(Status.CANCELLED.toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Booking not found")
        @WithMockUser(roles = "USER")
        void bookingNotFound_ShouldReturn404() throws Exception {
            when(bookingService.cancelBooking(bookingId)).thenThrow(new
                    EntityNotFoundException("Booking not found with id: " + bookingId));

            mockMvc.perform(delete(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Isn't owner trying to cancell booking")
        @WithMockUser(roles = "USER")
        void notOwner_shouldReturn409() throws Exception {
            when(bookingService.cancelBooking(bookingId)).thenThrow(new
                    IllegalStateException("You isn't owner of booking"));

            mockMvc.perform(delete(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Booking already cancelled")
        @WithMockUser(roles = "USER")
        void bookingAlreadyCancelled_ShouldReturn409() throws Exception {
            when(bookingService.cancelBooking(bookingId))
                    .thenThrow(new IllegalStateException("Booking is already cancelled"));

            mockMvc.perform(delete(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict());
        }
    }
}