package com.nooki.repository;

import com.nooki.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByCountryAndCityAndAddress(String country, String city, String address);

    Optional<Location> findByCountryAndCityAndAddress(String country, String city, String address);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO location (address, city, country)
        VALUES (:address, :city, :country)
        ON CONFLICT ON CONSTRAINT uniqueaddress DO NOTHING
    """, nativeQuery = true)
    void insertIgnore(@Param("country") String country,
                      @Param("city") String city,
                      @Param("address") String address);
}
