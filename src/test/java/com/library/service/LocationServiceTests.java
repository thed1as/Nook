package com.library.service;

import com.library.dto.location.LocationRequest;
import com.library.entity.Location;
import com.library.repository.LocationRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTests {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private LocationRepository locationRepository;

    @Test
    void createLocationOrGet_validRequest_ReturnsLocation(){
        String country = "USA";
        String city = "NY";
        String address = "Wall St.";
        LocationRequest request = new LocationRequest();
        request.setCountry(country);
        request.setCity(city);
        request.setAddress(address);

        Location existingLoc = new Location();
        existingLoc.setCountry(country.toLowerCase());
        existingLoc.setCity(city.toLowerCase());
        existingLoc.setAddress(address.toLowerCase());

        when(locationRepository.findByCountryAndCityAndAddress("usa", "ny", "wall st."))
                .thenReturn(Optional.of(existingLoc));

        Location result = locationService.createLocationOrGet(request);

        assertNotNull(result);
        assertEquals(existingLoc, result);

        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void createLocationOrGet_CreateNewLocation_ReturnsLocation() {
        LocationRequest request = new LocationRequest();
        request.setCountry("Germany");
        request.setCity("Berlin");
        request.setAddress("Main St.");

        when(locationRepository.findByCountryAndCityAndAddress("germany", "berlin", "main st."))
                .thenReturn(Optional.empty());

        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));

        Location result = locationService.createLocationOrGet(request);

        assertNotNull(result);
        assertEquals("germany", result.getCountry());
        assertEquals("berlin", result.getCity());
        assertEquals("main st.", result.getAddress());

        verify(locationRepository).save(any(Location.class));
    }
}
