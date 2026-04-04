package com.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "listing_image")
public class ListingImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID listingImageId;

    private String url;

//    Connections
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listingImg;
}
