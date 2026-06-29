package com.nooki.mapper;

import com.nooki.dto.review.ReviewRequest;
import com.nooki.dto.review.ReviewResponse;
import com.nooki.dto.review.UpdateReviewRequest;
import com.nooki.entity.Review;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "username", source = "user.username")
    ReviewResponse toReviewResponse(Review review);

    Review toReview(ReviewRequest reviewRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReview(UpdateReviewRequest updateReviewRequest, @MappingTarget Review review);
}
