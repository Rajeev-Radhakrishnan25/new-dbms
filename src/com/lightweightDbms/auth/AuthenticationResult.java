package com.lightweightDbms.auth;

/**
 * Represents the result of an authentication attempt by the user.
 */
public class AuthenticationResult {
    private final boolean success;
    private final String message;
    private final long previousSuccessfulLoginAt;

    /**
     * Constructs a new AuthenticationResult.
     *
     * @param success whether authentication was successful
     * @param message a descriptive message about the result
     */
    public AuthenticationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.previousSuccessfulLoginAt = 0L;
    }

    /**
     * Constructs a new AuthenticationResult including previous login time.
     *
     * @param success whether authentication was successful
     * @param message a descriptive message about the result
     * @param previousSuccessfulLoginAt previous successful login epoch millis (0 if none)
     */
    public AuthenticationResult(boolean success, String message, long previousSuccessfulLoginAt) {
        this.success = success;
        this.message = message;
        this.previousSuccessfulLoginAt = previousSuccessfulLoginAt;
    }

    /**
     * Checks if authentication was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the result message.
     *
     * @return the descriptive message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return previous successful login epoch millis (0 if none)
     */
    public long getPreviousSuccessfulLoginAt() { return previousSuccessfulLoginAt; }

    @Override
    public String toString() {
        return "AuthenticationResult{success=" + success + ", message='" + message + "'}";
    }
}
