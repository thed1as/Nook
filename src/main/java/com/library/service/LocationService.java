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

//    in function
    @Transactional
    public Location createLocationOrGet(LocationRequest request) {
        String country = request.getCountry().toLowerCase();
        String city = request.getCity().toLowerCase();
        String address = request.getAddress().toLowerCase();

        return locationRepository.findByCountryAndCityAndAddress(country, city, address)
                .orElseGet(() -> {
                    Location newLoc = new Location();
                    newLoc.setCountry(country);
                    newLoc.setCity(city);
                    newLoc.setAddress(address);
                    return locationRepository.save(newLoc);
                });
    }
}
