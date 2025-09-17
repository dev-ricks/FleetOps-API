package com.fleetops.exception;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Signals a business conflict where a vehicle's license plate must be unique but is already in use.
 * <p>
 * Typically mapped to HTTP 409 Conflict by the global exception handler.
 */
public class LicensePlateAlreadyExistsException extends DataIntegrityViolationException {

    public LicensePlateAlreadyExistsException(String message) {
        super(message);
    }
}
