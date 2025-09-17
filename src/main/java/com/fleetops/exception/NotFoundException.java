package com.fleetops.exception;

/**
 * Marker interface for not-found exceptions in the domain/service layer.
 * <p>
 * Implementations are typically mapped to HTTP 404 by the global exception handler.
 */
public interface NotFoundException {
}
