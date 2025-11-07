package com.lightweightDbms.auth;

import com.lightweightDbms.exception.PasswordHashException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Implements password hashing using SHA-256 algorithm.
 * This class is responsible for securely hashing passwords and verifying them.
 *
 */
public class SHA256PasswordHasher implements PasswordHasher {
    private static final String ALGORITHM = "SHA-256";

    /**
     * Hashes a password using SHA-256 algorithm.
     *
     * @param password the plain text password to hash
     * @return the hashed password as a hexadecimal string
     * @throws PasswordHashException if the SHA-256 algorithm is not available
     */
    @Override
    public String hashPassword(String password) throws PasswordHashException {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordHashException("Failed to hash password: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies if a plain text password matches a hashed password.
     *
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if passwords match, false otherwise
     * @throws PasswordHashException if verification fails
     */
    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) throws PasswordHashException {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(hashedPassword);
    }

    /**
     * Converts byte array to hexadecimal string.
     *
     * @param hash the byte array to convert
     * @return hexadecimal string representation
     */
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}