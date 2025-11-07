package com.lightweightDbms.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the creation, validation, and expiration of temporary security tokens.
 * These tokens are primarily used for password reset sessions to ensure secure and time-limited access.
 *
 * <p><b>Design:</b> Follows the Single Responsibility Principle (SRP) — handles only token-related operations.</p>
 * <p><b>Security:</b> Tokens are unique, randomly generated UUIDs and expire after a set duration.</p>
 */
public class TokenManager {

    /**
     * Inner class representing a token with its associated expiration time.
     */
    private static class TokenInfo {
        private final String token;
        private final LocalDateTime expirationTime;

        TokenInfo(String token, LocalDateTime expirationTime) {
            this.token = token;
            this.expirationTime = expirationTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }

    private static final Map<String, TokenInfo> tokenStorage = new HashMap<>();
    private static final int TOKEN_VALIDITY_MINUTES = 1; // Token valid for 1 minutes

    /**
     * Generates a secure, time-limited token for a given user ID.
     *
     * @param userId The user ID for whom the token is generated.
     * @return The generated token string.
     */
    public static String generateToken(String userId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);
        tokenStorage.put(userId, new TokenInfo(token, expiration));
        return token;
    }

    /**
     * Validates whether a given token is active and not expired for the specified user.
     *
     * @param userId The ID of the user attempting to use the token.
     * @param token The token string provided by the user.
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise.
     */
    public static boolean validateToken(String userId, String token) {
        TokenInfo info = tokenStorage.get(userId);
        if (info == null) return false;
        return info.token.equals(token) && !info.isExpired();
    }

    /**
     * Invalidates the token associated with a user once it has been used or expired.
     *
     * @param userId The user ID whose token should be invalidated.
     */
    public static void invalidateToken(String userId) {
        tokenStorage.remove(userId);
    }

    /**
     * Cleans up all expired tokens from memory.
     * This helps prevent token buildup for inactive users.
     */
    public static void cleanExpiredTokens() {
        tokenStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public static int getTokenExpirationTime() {
        return TOKEN_VALIDITY_MINUTES;
    }
}
