package com.fleetops.exception;

public abstract class NotFoundExceptionBase extends RuntimeException implements NotFoundException {

    public NotFoundExceptionBase(String message) {
        super(message);
    }
}
