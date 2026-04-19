package com.library.controller;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Booking", description = "Booking API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

//    CHANGE THAT USERNAME LATER TO AUTHENTICATION
    @Operation(summary = "Create booking")
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse br = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(br);
    }

    @Operation(summary = "Find booking by id")
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable UUID id) {
        BookingResponse br = bookingService.getBookingById(id);
        return ResponseEntity.ok(br);
    }

    @Operation(summary = "Find bookings")
    @GetMapping("/bookings/my")
    public ResponseEntity<List<BookingResponse>> getBookings() {
        List<BookingResponse> lbr = bookingService.getMyBookings();
        return ResponseEntity.ok(lbr);
    }

    @Operation(summary = "Find listing bookings")
    @GetMapping("/listings/{id}/bookings")
    public ResponseEntity<List<BookingResponse>> getListings(@PathVariable UUID id) {
        List<BookingResponse> lbr = bookingService.getListingBookings(id);
        return ResponseEntity.ok(lbr);
    }

    @Operation(summary = "Cancel booking")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> delete(@PathVariable UUID id) {
        BookingResponse br = bookingService.cancelBooking(id);
        return ResponseEntity.ok(br);
    }
}
