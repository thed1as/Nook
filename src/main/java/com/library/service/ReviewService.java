package com.library.service;

import com.library.dto.review.ReviewRequest;
import com.library.dto.review.ReviewResponse;
import com.library.dto.review.UpdateReviewRequest;
import com.library.entity.Listing;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.mapper.ReviewMapper;
import com.library.repository.BookingRepository;
import com.library.repository.ListingRepository;
import com.library.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final ReviewMapper reviewMapper;
    private final ListingService listingService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsOfListing(UUID listingId, Pageable pageable) {
        listingService.getListingOrThrow(listingId);
        return reviewRepository
                .findAllByListing_ListingIdOrderByCreatedAtDesc(listingId, pageable)
                .map(reviewMapper::toReviewResponse);
    }

    @Transactional
    public ReviewResponse addReview(ReviewRequest reviewRequest, UUID listingId) {
        User user = userService.getUserByEmail(userService.getCurrentUserEmail());
        Listing listing = listingService.getListingOrThrow(listingId);
        if(user.getUserId().equals(listing.getUser().getUserId())) {
            throw new IllegalStateException("You cannot add reviews to your listing");
        }

        if(!bookingRepository.existsBookingByListing_ListingIdAndUser_UserId(listingId, user.getUserId())) {
            throw new IllegalStateException("You must have a completed booking to leave a review");
        }

        if(reviewRepository.countAllByListing_ListingIdAndUser_UserId(listingId, user.getUserId()) >= 1) {
            throw new IllegalStateException("You can't review anymore");
        }

        Review review = Review.builder()
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .user(user)
                .listing(listing).build();

        user.addReview(review);
        listing.addReview(review);

        reviewRepository.save(review);

        long oldCount = listing.getReviewsCount();
        long newCount = oldCount + 1;

        BigDecimal newAvg = listing.getAverageRating()
                .multiply(BigDecimal.valueOf(oldCount))
                .add(review.getRating())
                .divide(BigDecimal.valueOf(newCount),2, RoundingMode.HALF_EVEN);
        listing.setReviewsCount(newCount);
        listing.setAverageRating(newAvg);
        listingRepository.save(listing);

//    I COULD CREATE TWO REVIEWS BUT I SHOULDN'T

        return reviewMapper.toReviewResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(UpdateReviewRequest updateReviewRequest, UUID listingId, UUID reviewId) {
        if(!listingRepository.existsById(listingId)) {
            throw new EntityNotFoundException("Listing not exists");
        }
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        if(!userService.getCurrentUserEmail().equals(review.getUser().getEmail())) {
            throw new IllegalStateException("Not your review!");
        }

        if(!updateReviewRequest.getRating().equals(review.getRating())) {
            Listing listing = review.getListing();
            BigDecimal newAvg = listing.getAverageRating()
                    .multiply(BigDecimal.valueOf(listing.getReviewsCount()))
                    .subtract(review.getRating())
                    .add(updateReviewRequest.getRating())
                    .divide(BigDecimal.valueOf(listing.getReviewsCount()), 2, RoundingMode.HALF_UP);

            listing.setAverageRating(newAvg);
            listingRepository.save(listing);
        }

        reviewMapper.updateReview(updateReviewRequest, review);

        return reviewMapper.toReviewResponse(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID listingId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);
        if(!review.getUser().getEmail().equals(userService.getCurrentUserEmail())) {
            throw new IllegalStateException("Not your review!");
        }
        Listing listing = listingService.getListingOrThrow(listingId);
        listing.removeReview(review);

        BigDecimal newAvg;
        Long oldCount = listing.getReviewsCount();
        BigDecimal currentAvg = listing.getAverageRating();
        BigDecimal ratingToDelete = review.getRating();

        if(oldCount > 1) {
            newAvg = currentAvg
                    .multiply(BigDecimal.valueOf(oldCount))
                    .subtract(ratingToDelete)
                    .divide(BigDecimal.valueOf(oldCount - 1), 2, RoundingMode.HALF_UP);
        } else {
            newAvg = BigDecimal.ZERO;
        }

        listing.setAverageRating(newAvg);
        listing.setReviewsCount(oldCount - 1);
        listingRepository.save(listing);
        reviewRepository.delete(review);
    }

}
