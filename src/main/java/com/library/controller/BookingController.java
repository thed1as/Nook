package com.library.controller;

import com.library.dto.booking.BookingResponse;
import com.library.dto.checkout.BookingCheckoutRequest;
import com.library.dto.checkout.BookingCheckoutResponse;
import com.library.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Booking", description = "Booking API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

//    CHANGE THAT USERNAME LATER TO AUTHENTICATION
    @Operation(summary = "Create booking")
    @PostMapping("/booking")
    public ResponseEntity<BookingCheckoutResponse> create(@Valid @RequestBody BookingCheckoutRequest bookingRequest) {
        BookingCheckoutResponse bcr = bookingService.createBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(bcr);
    }

    @Operation(summary = "Find booking by id")
    @GetMapping("/booking/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable UUID id) {
        BookingResponse br = bookingService.getBookingById(id);
        return ResponseEntity.ok(br);
    }

    @Operation(summary = "Find bookings")
    @GetMapping("/bookings/my")
    public ResponseEntity<Page<BookingResponse>> getBookings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookingResponse> lbr = bookingService.getMyBookings(pageable);
        return ResponseEntity.ok(lbr);
    }

    @Operation(summary = "Find listing bookings")
    @GetMapping("/listings/{id}/bookings")
    public ResponseEntity<Page<BookingResponse>> getListings(@PathVariable UUID id,
                                                             @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookingResponse> lbr = bookingService.getListingBookings(id, pageable);
        return ResponseEntity.ok(lbr);
    }

    @Operation(summary = "Cancel booking")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> delete(@PathVariable UUID id) {
        BookingResponse br = bookingService.cancelBooking(id);
        return ResponseEntity.ok(br);
    }
}
