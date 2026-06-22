package com.library.service;

import com.library.dto.exception.customException.LocationException.LocationException;
import com.library.dto.location.LocationRequest;
import com.library.entity.Location;
import com.library.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
                .orElseThrow(() -> new LocationException("Location not found"));
    }
}
