package com.nooki.service;

import com.nooki.dto.location.LocationRequest;
import com.nooki.entity.Location;
import com.nooki.repository.LocationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("create location")
    class CreateLocation {
        @Test
        @DisplayName("valid request should get location and return")
        void createLocationOrGet_validRequest_ReturnsLocation(){
            LocationRequest request = new LocationRequest();
            request.setCountry("USA");
            request.setCity("NY");
            request.setAddress("Wall St.");

            Location expected = new Location();
            expected.setCountry("usa");
            expected.setCity("ny");
            expected.setAddress("wall st.");

            when(locationRepository.findByCountryAndCityAndAddress("usa", "ny", "wall st."))
                    .thenReturn(Optional.of(expected));

            Location result = locationService.createLocationOrGet(request);

            assertNotNull(result);
            assertEquals("usa", result.getCountry());

            verify(locationRepository).insertIgnore("usa", "ny", "wall st.");
        }
    }
}
