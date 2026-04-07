package com.library.controller;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.service.ListingService;
//import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ListingController {
    private final ListingService listingService;

//    @Operation(summary = "Create listing")
    @PostMapping("/listings")
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody ListingRequest listingRequest,
                                             List<MultipartFile> files,
                                             String email) {
        ListingResponse lr = listingService.createListing(listingRequest, files, email);
        return ResponseEntity.ok(lr);
    }

//    @Operation(summary = "Find listings")
    @GetMapping("/listings/{id}")
    public ResponseEntity<ListingResponse> get(@PathVariable UUID id) {
        ListingResponse lr = listingService.getListingById(id);
        return ResponseEntity.ok(lr);
    }

//    @Operation(summary = "Find all listings")
    @GetMapping("/listings")
    public ResponseEntity<List<ListingResponse>> getListings() {
        List<ListingResponse> llr = listingService.getAll();
        return ResponseEntity.ok(llr);
    }
//  Get authentication later when you add security
//    @Operation(summary = "Update listing by id")
    @PutMapping("/listings/{id}")
    public ResponseEntity<ListingResponse> update(@PathVariable UUID id, @RequestBody UpdateListingRequest listingRequest, String email) {
        ListingResponse lr = listingService.updateListing(listingRequest, email, id);
        return ResponseEntity.ok(lr);
    }

//    @Operation(summary = "Delete listing by id")
    @DeleteMapping("/listings/{id}")
    public void delete(@PathVariable UUID id) {
        listingService.deleteListingById(id);
    }
}
