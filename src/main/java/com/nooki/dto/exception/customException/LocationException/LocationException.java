package com.nooki.dto.exception.customException.LocationException;

public class LocationException extends RuntimeException  {
    public LocationException(String message) {
        super(message);
    }

    public LocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
