package com.fleetops.exception;

/**
 * Wrapper for unexpected service-layer errors (e.g., data-access failures).
 * <p>
 * Typically mapped to HTTP 500 by the global exception handler.
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
