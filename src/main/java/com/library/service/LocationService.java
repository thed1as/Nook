package com.library.service;

import com.library.dto.location.LocationRequest;
import com.library.entity.Location;
import com.library.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

//    in function
    @Transactional
    public Location createLocationOrGet(LocationRequest request) {
        String country = request.getCountry().toLowerCase();
        String city = request.getCity().toLowerCase();
        String address = request.getAddress().toLowerCase();

        locationRepository.insertIgnore(country, city, address);

        return locationRepository.findByCountryAndCityAndAddress(country, city, address)
                .orElseThrow(() -> new IllegalStateException("Location not found"));
    }
}
