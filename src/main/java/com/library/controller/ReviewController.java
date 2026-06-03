package com.library.controller;

import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.dto.review.UpdateReviewRequest;
import com.library.service.ReviewService;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class  ReviewController {
    private final ReviewService reviewService;

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @GetMapping("/listing/{id}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(@PathVariable UUID id,
                                                           @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponse> prs = reviewService.getAllReviewsOfListing(id, pageable);
        return ResponseEntity.ok(prs);
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @PostMapping("/listing/{listingId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest reviewRequest,
                                                       @PathVariable UUID listingId) {
        ReviewResponse rr = reviewService.addReview(reviewRequest, listingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(rr);
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @PutMapping("/listing/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@Valid @RequestBody UpdateReviewRequest updateReviewRequest,
                                                       @PathVariable Long reviewId) {
        ReviewResponse rr = reviewService.updateReview(updateReviewRequest, reviewId);
        return ResponseEntity.ok(rr);
    }

    @PreAuthorize("hasRole('USER') or hasRole('HOST')")
    @DeleteMapping("/listing/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
