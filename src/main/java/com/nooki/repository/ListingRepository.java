package com.nooki.repository;

import com.nooki.entity.Listing;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {
    @Query("SELECT l FROM Listing l WHERE l.user.userId = :userId")
    List<Listing> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"location", "listingImages"})
    @Query("SELECT l FROM Listing l WHERE l.listingId IN :id")
    List<Listing> findAllDetailedByUserId(@Param("id") List<UUID> ids);

    @Query("SELECT l.listingId FROM Listing l WHERE l.user.userId = :userId")
    Page<UUID> findAllIdsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.listingId = :id")
    Optional<Listing> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT l FROM Listing l WHERE l.listingId = :id")
    @EntityGraph(attributePaths = {"user", "location", "listingImages"})
    Optional<Listing> findByDetailedId(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"location", "user"})
    @Query("SELECT l FROM Listing l WHERE l.listingId IN :ids")
    List<Listing> findAllByDetailedIds(@Param("ids") List<UUID> ids);

    @Query("SELECT l.listingId FROM Listing l")
    Page<UUID> findAllIds(Pageable pageable);

}
