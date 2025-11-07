package com.lightweightDbms.auth;

import com.lightweightDbms.exception.PasswordHashException;
import com.lightweightDbms.exception.UserNotFoundException;

/**
 * Interface for user password recovery operations.
 *
 */
public interface PasswordRecovery {

    /**
     * Initiates password recovery process by verifying the user's answer.
     *
     * @param userId  The user's ID.
     * @param answer  The user's provided answer to the stored question.
     * @return        A temporary token if verification succeeds.
     * @throws UserNotFoundException if verification fails.
     */
    String initiateRecovery(String userId, String answer) throws UserNotFoundException, IllegalArgumentException, PasswordHashException;

    int getTokenExpirationTime();

    /**
     * Resets user's password using a valid recovery token.
     *
     * @param token   Temporary reset token.
     * @param newPassword New password to set.
     */
    Boolean resetPassword(String token, String userId, String newPassword) throws PasswordHashException;

}