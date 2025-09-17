package com.fleetops.exception;

/**
 * Exception indicating a requested driver resource could not be found.
 * <p>
 * Mapped to HTTP 404 by the global controller exception handler.
 */
public class DriverNotFoundException extends NotFoundExceptionBase {

    public DriverNotFoundException(String message) {
        super(message);
    }
}
