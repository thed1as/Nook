package com.library.controller;

import com.library.dto.booking.BookingResponse;
import com.library.dto.checkout.BookingCheckoutRequest;
import com.library.dto.checkout.BookingCheckoutResponse;
import com.library.service.BookingServices.BookingQueryService;
import com.library.service.BookingServices.BookingCommandService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Booking", description = "Booking API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
    private final BookingCommandService commandService;
    private final BookingQueryService queryService;

//    CHANGE THAT USERNAME LATER TO AUTHENTICATION
    @Operation(summary = "Create booking")
    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @PostMapping("/booking")
    public ResponseEntity<BookingCheckoutResponse> create(@Valid @RequestBody BookingCheckoutRequest bookingRequest) {
        BookingCheckoutResponse bcr = commandService.create(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(bcr);
    }

    @Operation(summary = "Find booking by id")
    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @GetMapping("/booking/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable UUID id) {
        BookingResponse br = queryService.getBookingById(id);
        return ResponseEntity.ok(br);
    }

    @Operation(summary = "Find bookings")
    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @GetMapping("/bookings/my")
    public ResponseEntity<Page<BookingResponse>> getBookings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookingResponse> lbr = queryService.getMyBookings(pageable);
        return ResponseEntity.ok(lbr);
    }

    @Operation(summary = "Find listing bookings")
    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @GetMapping("/listings/{id}/bookings")
    public ResponseEntity<Page<BookingResponse>> getListings(@PathVariable UUID id,
                                                             @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BookingResponse> lbr = queryService.getListingBookings(id, pageable);
        return ResponseEntity.ok(lbr);
    }

    @Operation(summary = "Cancel booking")
    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> cancel(@PathVariable UUID id) {
        BookingResponse br = commandService.cancel(id);
        return ResponseEntity.ok(br);
    }
}
