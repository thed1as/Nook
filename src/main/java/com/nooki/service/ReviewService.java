package com.nooki.service;

import com.nooki.dto.exception.customException.reviewException.ReviewIllegalStateException;
import com.nooki.dto.exception.customException.reviewException.ReviewNotFoundException;
import com.nooki.dto.review.ReviewRequest;
import com.nooki.dto.review.ReviewResponse;
import com.nooki.dto.review.UpdateReviewRequest;
import com.nooki.entity.Listing;
import com.nooki.entity.Review;
import com.nooki.entity.User;
import com.nooki.enums.Status;
import com.nooki.mapper.ReviewMapper;
import com.nooki.repository.BookingRepository;
import com.nooki.repository.ListingRepository;
import com.nooki.repository.ReviewRepository;
import com.nooki.repository.UserRepository;
import com.nooki.service.ListingServices.ListingDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ListingDomainService listingDomainService;

    @Transactional(readOnly = true)
    @Cacheable(value = "listing_reviews", key = "#root.methodName + '_' + #listingId + '_' + #pageable")
    public Page<ReviewResponse> getAllReviewsOfListing(UUID listingId, Pageable pageable) {
        listingRepository.findById(listingId).orElseThrow(() -> new ReviewNotFoundException("listing not found"));
        return reviewRepository
                .findAllByListing_ListingIdOrderByCreatedAtDesc(listingId, pageable)
                .map(reviewMapper::toReviewResponse);
    }

    @Transactional
    public ReviewResponse addReview(ReviewRequest reviewRequest, UUID listingId) {
        UUID userId = userService.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        Listing listing = listingDomainService.getListingOrThrow(listingId);
        if(userId.equals(listing.getUser().getUserId())) {
            throw new ReviewIllegalStateException("You cannot add reviews to your listing");
        }

        if(!bookingRepository.existsByListing_ListingIdAndUser_UserIdAndStatus(listingId, userId, Status.COMPLETED)) {
            throw new ReviewIllegalStateException("You must have a completed booking to leave a review");
        }

        if(reviewRepository.countAllByListing_ListingIdAndUser_UserId(listingId, userId) >= 1) {
            throw new ReviewIllegalStateException("You can't review anymore");
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

        log.info("Added review {} to listing {}", review.getReviewId(), listing.getListingId());

        return reviewMapper.toReviewResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(UpdateReviewRequest updateReviewRequest, Long reviewId) {
        Review review = reviewRepository.findByDetailedReviewId(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("review not found"));
        UUID currUserId = userService.getCurrentUserId();
        if(!currUserId.equals(review.getUser().getUserId())) {
            log.warn("User {} tried to update not his review {}", userService.getCurrentUserId(), reviewId);
            throw new ReviewIllegalStateException("Not your review!");
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

        log.info("Review {} was successfully updated by userId {}", reviewId, currUserId);
        return reviewMapper.toReviewResponse(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findByDetailedReviewId(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("RReview not found"));
        UUID userId = userService.getCurrentUserId();
        if(!review.getUser().getUserId().equals(userId)) {
            log.warn("User {} tried to delete a review {} that was not theirs", userId, reviewId);
            throw new ReviewIllegalStateException("Not your review!");
        }
        Listing listing = review.getListing();
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
            log.debug("Recalculating listing {} avg rating. old avg: {} new avg: {}", listing.getListingId(), currentAvg, newAvg);
        } else {
            newAvg = BigDecimal.ZERO;
        }

        listing.setAverageRating(newAvg);
        listing.setReviewsCount(oldCount - 1);
        listingRepository.save(listing);
        reviewRepository.deleteByDetailedId(review.getReviewId());
        log.info("Successfully deleted the review {}", reviewId);
    }

}
