package com.library.repository;

import com.library.entity.Listing;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {
    @Query("SELECT l FROM Listing l WHERE l.user.userId = :userId")
    List<Listing> findAllByUserId(UUID userId);

    @Query("SELECT l FROM Listing l WHERE l.user.email = :email")
    @EntityGraph(attributePaths = {"location"})
    List<Listing> findAllByUserEmail(String email);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.listingId = :id")
    Optional<Listing> findByIdWithLock(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.listingId = :id")
    @EntityGraph(attributePaths = {"user", "location"})
    Optional<Listing> findByDetailedIdWithLock(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"location", "user"})
    Page<Listing> findAll(Pageable pageable);

    @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.location LEFT JOIN FETCH l.user WHERE l.listingId IN :ids")
    List<Listing> findAllWithRelationByIds(@Param("ids") List<UUID> listingIds);

    @Modifying
    @Query("DELETE FROM Listing l WHERE l.listingId = :id")
    void deleteDetailedById(@Param("listingId") UUID listingId);
}
