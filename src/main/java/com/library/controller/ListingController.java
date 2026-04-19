package com.library.controller;

import com.library.dto.listing.ListingRequest;
import com.library.dto.listing.ListingResponse;
import com.library.dto.listing.UpdateListingRequest;
import com.library.service.ListingService;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Tag(name = "Listing", description = "Listing API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ListingController {
    private final ListingService listingService;
    private final UserService userService;

//    CREATING

    @Operation(summary = "Create listing")
    @PreAuthorize("hasRole('HOST')")
    @PostMapping(value = "/listings")
    public ResponseEntity<ListingResponse> create(
            @Parameter(description = "Данные о листинге")
            @Valid @RequestBody ListingRequest listingRequest)
    {
        String email = userService.getCurrentUserEmail();
        ListingResponse lr = listingService.createListing(listingRequest, email);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Add image to the listing")
    @PreAuthorize("hasRole('HOST')")
    @PostMapping(value = "/listings/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingResponse> addImageToListing(
            @Parameter(description = "Изображение (jpg, png)")
            @RequestParam("files") List<MultipartFile> files,

            @PathVariable UUID id) {
        ListingResponse lr = listingService.addImageToListing(id, files);
        return ResponseEntity.ok(lr);
    }

    //    SEARCHING

    @Operation(summary = "Find listings")
    @GetMapping("/listings/{id}")
    public ResponseEntity<ListingResponse> get(@PathVariable UUID id) {
        ListingResponse lr = listingService.getListingById(id);
        return ResponseEntity.ok(lr);
    }


    @Operation(summary = "Find users listings by id")
    @PreAuthorize("hasRole('HOST')")
    @GetMapping("/listings/my")
    public ResponseEntity<List<ListingResponse>> getUserListings() {
        String email = userService.getCurrentUserEmail();
        List<ListingResponse> lr = listingService.getUsersListings(email);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Find all listings")
    @GetMapping("/listings")
    public ResponseEntity<Page<ListingResponse>> getListings(Pageable pageable) {
        Page<ListingResponse> llr = listingService.getAll(pageable);
        return ResponseEntity.ok(llr);
    }

//    UPDATE

    @Operation(summary = "Update listing by id")
    @PreAuthorize("hasRole('HOST')")
    @PutMapping("/listings/{id}")
    public ResponseEntity<ListingResponse> update(@PathVariable UUID id, @RequestBody UpdateListingRequest listingRequest) {
        String email = userService.getCurrentUserEmail();
        ListingResponse lr = listingService.updateListing(listingRequest, email, id);
        return ResponseEntity.ok(lr);
    }

//    DELETE

    @Operation(summary = "Delete listing by id")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/listings/{id}")
    public void delete(@PathVariable UUID id) {
        listingService.deleteListingById(id);
    }
}
