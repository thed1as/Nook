package com.library.controller;

import com.library.config.SecurityConfig;
import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.dto.checkout.BookingCheckoutRequest;
import com.library.dto.payment.PaymentRequest;
import com.library.enums.PaymentMethod;
import com.library.enums.Status;
import com.library.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(1).with(LocalTime.NOON));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(5).with(LocalTime.NOON));

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            paymentRequest.setCurrency("USD");

            BookingCheckoutRequest bookingCheckoutRequest = new BookingCheckoutRequest();
            bookingCheckoutRequest.setBookingRequest(bookingRequest);
            bookingCheckoutRequest.setPaymentRequest(paymentRequest);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingCheckoutRequest))
            ).andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Return 400, if ID field is empty (valid)")
        @WithMockUser(roles = "USER")
        void incorrectListingIdRequest_ShouldReturn400() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(null);
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(1).with(LocalTime.NOON));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(5).with(LocalTime.NOON));

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            paymentRequest.setCurrency("USD");

            BookingCheckoutRequest bookingCheckoutRequest = new BookingCheckoutRequest();
            bookingCheckoutRequest.setBookingRequest(bookingRequest);
            bookingCheckoutRequest.setPaymentRequest(paymentRequest);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingCheckoutRequest))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Return 400, if ID field is empty (valid)")
        @WithMockUser(roles = "USER")
        void blankPaymentRequestCurrency_ShouldReturn400() throws Exception {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setListingId(UUID.randomUUID());
            bookingRequest.setCheckInDate(LocalDateTime.now().plusDays(1).with(LocalTime.NOON));
            bookingRequest.setCheckOutDate(LocalDateTime.now().plusDays(5).with(LocalTime.NOON));

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            BookingCheckoutRequest bookingCheckoutRequest = new BookingCheckoutRequest();
            bookingCheckoutRequest.setBookingRequest(bookingRequest);
            bookingCheckoutRequest.setPaymentRequest(paymentRequest);

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingCheckoutRequest))
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

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            paymentRequest.setCurrency("USD");

            BookingCheckoutRequest bookingCheckoutRequest = new BookingCheckoutRequest();
            bookingCheckoutRequest.setBookingRequest(bookingRequest);
            bookingCheckoutRequest.setPaymentRequest(paymentRequest);

            when(bookingService.createBooking(any(BookingCheckoutRequest.class)))
                    .thenThrow(new IllegalStateException("Invalid date range"));

            mockMvc.perform(post(URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingCheckoutRequest))
            ).andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Return 403, if anonymous user trying to book")
        void anonymousUser_ShouldReturn403() throws Exception {
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
            Page<BookingResponse> brp = new PageImpl<>(brl);

            when(bookingService.getMyBookings(any(Pageable.class))).thenReturn(brp);

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

            Pageable pageable = PageRequest.of(0, 1);
            Page<BookingResponse> brp = new PageImpl<>(brl, pageable, brl.size());

            when(bookingService.getListingBookings(listingId, pageable)).thenReturn(brp);
            mockMvc.perform(get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
            ).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Cancelling booking (DELETEMAPPING /bookings/{id})")
    class CancelBooking {
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