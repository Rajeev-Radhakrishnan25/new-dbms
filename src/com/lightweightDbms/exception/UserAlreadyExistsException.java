package com.lightweightDbms.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 *
 */
public class UserAlreadyExistsException extends Exception {
    /**
     * Constructs a new UserAlreadyExistsException with the specified message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
