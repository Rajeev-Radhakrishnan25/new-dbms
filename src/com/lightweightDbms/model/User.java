package com.lightweightDbms.model;

import java.util.Objects;

/**
 * Represents a user in the database management system.
 * This class encapsulates user credentials and provides methods for user management.
 *
 */
public class User {
    private final String userId;
    private String hashedPassword;
    private final String securityQuestion;
    private final String securityAnswer;
    private final long createdAt;
    private boolean admin;
    private long lastSuccessfulLoginAt;

    /**
     * Constructs a new User with the specified credentials.
     *
     * @param userId the unique identifier for the user
     * @param hashedPassword the SHA-256 hashed password
     * @throws IllegalArgumentException if any value is null or empty
     */
    public User(String userId, String hashedPassword, String securityQuestion, String securityAnswer) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        if (securityQuestion == null || securityAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("Security Question cannot be null or empty");
        }
        if (securityAnswer == null || securityAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("Security Answer cannot be null or empty");
        }
        this.userId = userId.trim();
        this.hashedPassword = hashedPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.createdAt = System.currentTimeMillis();
        this.admin = false;
        this.lastSuccessfulLoginAt = 0L;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the hashed password.
     *
     * @return the SHA-256 hashed password
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {this.hashedPassword = hashedPassword;
    }

    /**
     * Gets the hashed security question.
     *
     * @return the SHA-256 hashed security question
     */
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    /**
     * Gets the hashed security answer.
     *
     * @return the SHA-256 security answer
     */
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    /**
     * Gets the timestamp when the user was created.
     *
     * @return the creation timestamp in milliseconds
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Indicates whether user has admin privileges.
     * @return true if admin
     */
    public boolean isAdmin() { return admin; }

    /**
     * Sets admin flag.
     * @param admin new admin value
     */
    public void setAdmin(boolean admin) { this.admin = admin; }

    /**
     * @return epoch millis of last successful login (0 if never)
     */
    public long getLastSuccessfulLoginAt() { return lastSuccessfulLoginAt; }

    /**
     * Updates last successful login timestamp.
     * @param timestampMillis epoch millis
     */
    public void setLastSuccessfulLoginAt(long timestampMillis) { this.lastSuccessfulLoginAt = timestampMillis; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', createdAt=" + createdAt + "}";
    }
}