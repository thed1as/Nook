package com.library.service;

import com.library.dto.location.LocationRequest;
import com.library.entity.Location;
import com.library.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public boolean existsByCountryAndCityAndAddress(String country, String city, String address) {
       return locationRepository.existsByCountryAndCityAndAddress(country, city, address);
    }

//    in function
    @Transactional
    public Location createLocationOrGet(LocationRequest locationRequest) {
        Location loc = locationRepository.findByCountryAndCityAndAddress(
                locationRequest.getCountry(),
                locationRequest.getCity(),
                locationRequest.getAddress()).orElse(null);
        if(loc != null) {
            return loc;
        }

        Location location = new Location();
        location.setCountry(locationRequest.getCountry());
        location.setCity(locationRequest.getCity());
        location.setAddress(locationRequest.getAddress());
        return location;
    }

    @Transactional(readOnly = true)
    public Location findById(UUID id) {
        return locationRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }
}
