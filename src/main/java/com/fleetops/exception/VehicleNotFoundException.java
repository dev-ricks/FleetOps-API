package com.fleetops.exception;

/**
 * Exception indicating a requested vehicle resource could not be found.
 * <p>
 * Mapped to HTTP 404 by the global controller exception handler.
 */
public class VehicleNotFoundException extends NotFoundExceptionBase {

    public VehicleNotFoundException(String message) {
        super(message);
    }
}
