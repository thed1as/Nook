package com.library.repository;

import com.library.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    boolean existsByCountryAndCityAndAddress(String country, String city, String address);

    Optional<Location> findByCountryAndCityAndAddress(String country, String city, String address);
}
