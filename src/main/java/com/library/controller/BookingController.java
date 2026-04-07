package com.library.controller;

import com.library.dto.booking.BookingRequest;
import com.library.dto.booking.BookingResponse;
import com.library.service.BookingService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//@Tag(name = "Booking", description = "Booking API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

//    CHANGE THAT USERNAME LATER TO AUTHENTICATION
//    @Operation(summary = "Create booking")
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest bookingRequest, String email) {
        BookingResponse br = bookingService.createBooking(bookingRequest, email);
        return ResponseEntity.ok(br);
    }

//    @Operation(summary = "Find booking by id")
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable UUID id) {
        BookingResponse br = bookingService.getBookingById(id);
        return ResponseEntity.ok(br);
    }

//    @Operation(summary = "Find bookings")
    @GetMapping("/users/{id}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookings(@PathVariable UUID id) {
        List<BookingResponse> lbr = bookingService.getUserBookings(id);
        return ResponseEntity.ok(lbr);
    }

//    @Operation(summary = "Find listing bookings")
    @GetMapping("/listings/{id}/bookings")
    public ResponseEntity<List<BookingResponse>> getListings(@PathVariable UUID id) {
        List<BookingResponse> lbr = bookingService.getListingBookings(id);
        return ResponseEntity.ok(lbr);
    }

//    @Operation(summary = "Cancel booking")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> delete(@PathVariable UUID id, String email) {
        BookingResponse br = bookingService.cancelBooking(id, email);
        return ResponseEntity.ok(br);
    }
}
