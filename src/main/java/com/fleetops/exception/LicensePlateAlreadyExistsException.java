package com.fleetops.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class LicensePlateAlreadyExistsException extends DataIntegrityViolationException {

    public LicensePlateAlreadyExistsException(String message) {
        super(message);
    }
}
