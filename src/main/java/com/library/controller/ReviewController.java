package com.library.controller;

import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.dto.review.UpdateReviewRequest;
import com.library.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class  ReviewController {
    private final ReviewService reviewService;

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @GetMapping("/listing/{id}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(@PathVariable UUID id, Pageable pageable) {
        Page<ReviewResponse> prs = reviewService.getAllReviewsOfListing(id, pageable);
        return ResponseEntity.ok(prs);
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @PostMapping("/listing/{id}/reviews")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest reviewRequest,
                                                       @PathVariable UUID id) {
        ReviewResponse rr = reviewService.addReview(reviewRequest, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(rr);
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @PutMapping("/listing/{listingId}/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@Valid @RequestBody UpdateReviewRequest updateReviewRequest,
                                                       @PathVariable UUID listingId,
                                                       @PathVariable UUID reviewId) {
        ReviewResponse rr = reviewService.updateReview(updateReviewRequest, reviewId, listingId);
        return ResponseEntity.ok(rr);
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @DeleteMapping("/listing/{listingId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID listingId,
                                                       @PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId, listingId);
        return ResponseEntity.noContent().build();
    }
}
