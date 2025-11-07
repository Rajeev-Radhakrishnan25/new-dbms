package com.lightweightDbms.auth;

import com.lightweightDbms.exception.PasswordHashException;

/**
 * Interface for password hashing operations.
 * This interface follows the Interface Segregation Principle by defining
 * only password hashing related operations.
 *
 */
public interface PasswordHasher {
    /**
     * Hashes a password using a specific algorithm.
     *
     * @param password the plain text password to hash
     * @return the hashed password as a hexadecimal string
     * @throws PasswordHashException if hashing fails
     */
    String hashPassword(String password) throws PasswordHashException;

    /**
     * Verifies if a plain text password matches a hashed password.
     *
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if passwords match, false otherwise
     * @throws PasswordHashException if verification fails
     */
    boolean verifyPassword(String plainPassword, String hashedPassword) throws PasswordHashException;
}