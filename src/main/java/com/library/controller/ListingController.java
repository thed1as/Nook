package com.library.controller;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Listing", description = "Listing API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ListingController {
    private final ListingService listingService;

    @Operation(summary = "Create listing")
    @PostMapping(value = "/listings")
    public ResponseEntity<ListingResponse> create(
            @Parameter(description = "Данные о листинге")
            @Valid @RequestBody ListingRequest listingRequest,

            @Parameter(description = "Email пользователя")
            String email) {
        ListingResponse lr = listingService.createListing(listingRequest, email);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Add image to the listing")
    @PostMapping(value = "/listings/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingResponse> addImageToListing(
            @Parameter(description = "Изображение (jpg, png)")
            @RequestParam("files") List<MultipartFile> files,

            @PathVariable UUID id) {
        ListingResponse lr = listingService.addImageToListing(id, files);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Find listings")
    @GetMapping("/listings/{id}")
    public ResponseEntity<ListingResponse> get(@PathVariable UUID id) {
        ListingResponse lr = listingService.getListingById(id);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Find all listings")
    @GetMapping("/listings")
    public ResponseEntity<List<ListingResponse>> getListings() {
        List<ListingResponse> llr = listingService.getAll();
        return ResponseEntity.ok(llr);
    }
//  Get authentication later when you add security
    @Operation(summary = "Update listing by id")
    @PutMapping("/listings/{id}")
    public ResponseEntity<ListingResponse> update(@PathVariable UUID id, @RequestBody UpdateListingRequest listingRequest, String email) {
        ListingResponse lr = listingService.updateListing(listingRequest, email, id);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Delete listing by id")
    @DeleteMapping("/listings/{id}")
    public void delete(@PathVariable UUID id) {
        listingService.deleteListingById(id);
    }
}
