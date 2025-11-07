package com.lightweightDbms.exception;

/**
 * Exception thrown when attempting to use a user which does not found.
 *
 */
public class UserNotFoundException extends Exception {
    /**
     * Constructs a new UserNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}