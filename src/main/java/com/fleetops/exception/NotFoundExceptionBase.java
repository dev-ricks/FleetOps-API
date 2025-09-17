package com.fleetops.exception;

/**
 * Base type for not-found exceptions in the domain/service layer.
 * <p>
 * Handled by {@code GlobalControllerExceptionHandler} and typically mapped to HTTP 404.
 */
public abstract class NotFoundExceptionBase extends RuntimeException implements NotFoundException {

    public NotFoundExceptionBase(String message) {
        super(message);
    }
}
