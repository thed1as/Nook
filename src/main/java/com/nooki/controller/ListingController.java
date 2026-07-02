package com.nooki.controller;

import com.nooki.dto.PageResponse;
import com.nooki.dto.listing.*;
import com.nooki.service.ListingServices.ListingQueryService;
import com.nooki.service.ListingServices.ListingCommandService;
import com.nooki.service.ListingServices.ListingImageService;
import com.nooki.service.ListingServices.ListingSpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ListingController {
    private final ListingQueryService listingQueryService;
    private final ListingCommandService listingCommandService;
    private final ListingImageService listingImageService;
    private final ListingSpecificationService listingSpecificationService;

//    CREATING

    @Operation(summary = "Create listing")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/listing")
    public ResponseEntity<ListingResponse> create(
            @Parameter(description = "Данные о листинге")
            @Valid @RequestBody ListingRequest listingRequest)
    {
        ListingResponse lr = listingCommandService.createListing(listingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(lr);
    }

    @Operation(summary = "Add image to the listing")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/listing/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingResponse> addImageToListing(
            @Parameter(description = "Изображение (jpg, png)")
            @RequestParam("files") List<MultipartFile> files,

            @PathVariable UUID id) {
        ListingResponse lr = listingImageService.addImageToListing(id, files);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Delete image from the listing")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value = "/listing/{listingId}/images/{imageId}")
    public ResponseEntity<Void> deleteImageFromListing(@PathVariable UUID listingId,
                                                                  @PathVariable Long imageId) {
        listingImageService.removeImage(listingId, imageId);
        return ResponseEntity.noContent().build();
    }

    //    SEARCHING

    @Operation(summary = "Find listing")
    @GetMapping("/listing/{id}")
    public ResponseEntity<FullListingResponse> get(@PathVariable UUID id,
                                                   @RequestParam(required = false, defaultValue = "USD") String currency) {
        FullListingResponse lr = listingQueryService.getListingById(id, currency);
        return ResponseEntity.ok(lr);
    }


    @Operation(summary = "Find users listings by id")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/listings/my")
    public ResponseEntity<Page<ListingResponse>> getUserListings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "USD") String currency) {
        Page<ListingResponse> lr = listingQueryService.getUsersListings(pageable, currency);
        return ResponseEntity.ok(lr);
    }

    @Operation(summary = "Find all listings")
    @GetMapping("/listings")
    public ResponseEntity<PageResponse<ShortListingResponse>> getListings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        PageResponse<ShortListingResponse> llr = listingQueryService.getAll(pageable);
        return ResponseEntity.ok(llr);
    }

    @Operation(summary = "find all by filter")
    @GetMapping("/listings/search")
    public ResponseEntity<PageResponse<ListingResponse>> getListingsByFilter(
            @RequestParam(required = false, defaultValue = "USD") String currency,
            @Valid ListingFilterRequest listingFilterRequest,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<ListingResponse> llr = listingSpecificationService.getListingsByFilter(currency, listingFilterRequest, pageable);

        return ResponseEntity.ok(llr);
    }

//    UPDATE

    @Operation(summary = "Update listing by id")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/listing/{id}")
    public ResponseEntity<ListingResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateListingRequest listingRequest) {
        ListingResponse lr = listingCommandService.updateListing(listingRequest, id);
        return ResponseEntity.ok(lr);
    }

//    DELETE

    @Operation(summary = "Delete listing by id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping("/listing/{id}")
    public void delete(@PathVariable UUID id) {
        listingCommandService.deleteListingById(id);
    }
}
