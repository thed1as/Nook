package com.nooki.entity;

import com.nooki.enums.listingReport.ListingReportReason;
import com.nooki.enums.listingReport.ListingReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter @Builder @AllArgsConstructor
@NoArgsConstructor
@Table(name ="listing_report")
public class ListingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    private ListingReportReason reason;

    private String description;

    @Enumerated(EnumType.STRING)
    private ListingReportStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    private String reviewedBy;

    @Version
    private Long version;

//    Links
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_reporter")
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

}
