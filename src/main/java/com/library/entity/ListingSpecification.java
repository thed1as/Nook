package com.library.entity;

import com.library.dto.listing.ListingFilterRequest;
import com.library.enums.Status;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ListingSpecification {
    public static Specification<Listing> build(ListingFilterRequest filter) {
        return (root, query, cb) -> {
            if(query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
                root.fetch("location", JoinType.LEFT);
            }

            return Specification.allOf(
                    priceGreaterThanOrEqualTo(filter.getMinPrice()),
                    priceLessThanOrEqualTo(filter.getMaxPrice()),
                    hasCountry(filter.getCountry()),
                    hasCity(filter.getCity()),
                    isAvailableBetween(filter.getCheckIn(), filter.getCheckOut())
            ).toPredicate(root, query, cb);
        };
    }

    private static Specification<Listing> priceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null :
                cb.greaterThanOrEqualTo(root.get("pricePerNight"), minPrice);
    }

    private static Specification<Listing> priceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null :
                cb.lessThanOrEqualTo(root.get("pricePerNight"), maxPrice);
    }

    private static Specification<Listing> hasCountry(String country) {
        return (root, query, cb) -> {
            if(!StringUtils.hasText(country)) return null;
            Join<Listing, Location> join = root.join("location", JoinType.LEFT);
            String countryPattern = "%" + country.toLowerCase() + "%";
            return cb.like(cb.lower(join.get("country")), countryPattern);
        };
    }

    private static Specification<Listing> hasCity(String city) {
        return (root, query, cb) -> {
            if(!StringUtils.hasText(city)) return null;
            Join<Listing, Location> join = root.join("location", JoinType.LEFT);
            String cityPattern = "%" + city.toLowerCase() + "%";
            return cb.like(cb.lower(join.get("city")), cityPattern);
        };
    }

    private static Specification<Listing> isAvailableBetween(LocalDateTime checkIn, LocalDateTime checkOut) {
        return (root, query, cb) -> {

            if(checkIn == null || checkOut == null) return null;

            if(!checkOut.isAfter(checkIn)) {
                throw new IllegalStateException("Invalid date range");
            }

            Subquery<UUID> bookingSubquery = query.subquery(UUID.class);
            Root<Booking> bookingRoot = bookingSubquery.from(Booking.class);

            var overlapPredicate = cb.and(
                    cb.lessThan(bookingRoot.get("checkInDate"), checkOut),
                    cb.greaterThan(bookingRoot.get("checkOutDate"), checkIn)
            );

            var activePredicate = cb.notEqual(bookingRoot.get("status"), Status.CANCELLED);

            bookingSubquery.select(bookingRoot.get("bookingId")
            ).where(
                    cb.equal(bookingRoot.get("listing"), root),
                    overlapPredicate,
                    activePredicate
            );
            return cb.not(cb.exists(bookingSubquery));

        };
    }
}
