package com.fleetops.exception;

/**
 * Exception indicating a requested inspection resource could not be found.
 * <p>
 * Mapped to HTTP 404 by the global controller exception handler.
 */
public class InspectionNotFoundException extends NotFoundExceptionBase {

    public InspectionNotFoundException(String message) {
        super(message);
    }
}
