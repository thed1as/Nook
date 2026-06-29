package com.nooki.mapper;

import com.nooki.dto.location.LocationRequest;
import com.nooki.dto.location.LocationResponse;
import com.nooki.entity.Location;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationResponse toLocationResponse(Location location);

    Location toLocation(LocationRequest locationRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLocation(LocationRequest req, @MappingTarget Location location);
}
