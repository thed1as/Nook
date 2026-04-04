package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter @Builder @AllArgsConstructor
@NoArgsConstructor
@Table(name = "listing")
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID listingId;

    private String title;

    private String description;

    private BigDecimal pricePerNight;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


//    Connections

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "listing")
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "listing")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "listingImg", cascade = CascadeType.ALL)
    private List<ListingImage> listingImages = new ArrayList<>();


//    helper methods
//    booking
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setListing(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setListing(null);
    }

//    reviews
    public void addReview(Review review) {
        reviews.add(review);
        review.setListing(this);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        review.setListing(null);
    }

//    listingImages
    public void addListingImage(List<ListingImage> listingImage) {
        for(ListingImage i : listingImage) {
            listingImages.add(i);
            i.setListingImg(this);
        }
    }

    public void addListingImage(ListingImage listingImage) {
        listingImages.add(listingImage);
        listingImage.setListingImg(this);
    }

    public void removeListingImage(ListingImage listingImage) {
        listingImages.remove(listingImage);
        listingImage.setListingImg(null);
    }
}
