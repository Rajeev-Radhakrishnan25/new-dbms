package com.lightweightDbms.auth;

import com.lightweightDbms.exception.PasswordHashException;
import com.lightweightDbms.exception.UserNotFoundException;
import com.lightweightDbms.model.User;
import com.lightweightDbms.repository.UserRepository;

/**
 * Service responsible for managing password recovery and reset process.
 * Handles security questions, answer verification, and temporary reset tokens.
 */
public class PasswordRecoveryService implements PasswordRecovery {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenManager tokenManager;

    public PasswordRecoveryService(UserRepository userRepository,
                                   PasswordHasher passwordHasher,
                                   TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenManager = tokenManager;
    }

    /**
     * Initiates password recovery process by verifying the user's answer.
     *
     * @param userId  The user's ID.
     * @param answer  The user's provided answer to the stored question.
     * @return        A temporary token if verification succeeds.
     * @throws IllegalArgumentException if verification fails.
     * @throws UserNotFoundException if User not found
     */
    public String initiateRecovery(String userId, String answer)
            throws UserNotFoundException, IllegalArgumentException, PasswordHashException {
        User user = userRepository.getUserById(userId);
        if (user == null) throw new UserNotFoundException("User not found.");

        String hashedAnswer = passwordHasher.hashPassword(answer);
        if (!hashedAnswer.equalsIgnoreCase(user.getSecurityAnswer())) {
            throw new IllegalArgumentException("Incorrect security answer.");
        }

        return tokenManager.generateToken(userId);
    }

    /**
     * Resets user's password using a valid recovery token.
     *
     * @param token     Temporary reset token.
     * @param newPassword   New password to set.
     */
    public Boolean resetPassword(String token, String userId, String newPassword)
            throws PasswordHashException {
        Boolean isValid = tokenManager.validateToken(userId, token);
        if (!isValid) throw new IllegalArgumentException("Invalid or expired token.");

        User user = userRepository.getUserById(userId);
        String hashedPassword = passwordHasher.hashPassword(newPassword);
        user.setHashedPassword(hashedPassword);
        tokenManager.invalidateToken(token);
        return true;
    }

    public int getTokenExpirationTime(){
        return tokenManager.getTokenExpirationTime();
    }
}
