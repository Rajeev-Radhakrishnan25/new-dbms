package com.lightweightDbms.exception;

/**
 * Exception thrown when password hashing operations fail.
 *
 */
public class PasswordHashException extends Exception {
    /**
     * Constructs a new PasswordHashException with the specified message.
     *
     * @param message the detail message
     */
    public PasswordHashException(String message) {
        super(message);
    }

    /**
     * Constructs a new PasswordHashException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public PasswordHashException(String message, Throwable cause) {
        super(message, cause);
    }
}