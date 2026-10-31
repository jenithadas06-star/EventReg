package com.college.events.service;

/**
 * Thrown when a business rule is violated (duplicate registration, event full,
 * student not eligible, ...). The message is safe to show to the user.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
