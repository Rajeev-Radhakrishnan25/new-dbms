package com.lightweightDbms.auth;

import com.lightweightDbms.audit.AuditLogger;
import com.lightweightDbms.audit.AuditRecord;
import com.lightweightDbms.audit.IpProvider;
import com.lightweightDbms.exception.PasswordHashException;
import com.lightweightDbms.exception.UserAlreadyExistsException;
import com.lightweightDbms.exception.UserNotFoundException;
import com.lightweightDbms.model.User;
import com.lightweightDbms.repository.UserRepository;

/**
 * Service class responsible for handling two-factor authentication
 * including password verification and CAPTCHA validation.
 *
 */
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CaptchaGenerator captchaGenerator;
    private final SecurityQuestions securityQuestions;
    private final PasswordRecovery passwordRecovery;
    private final AuditLogger auditLogger;
    private final IpProvider ipProvider;

    /**
     * Constructs a new AuthenticationService with required dependencies.
     *
     * @param userRepository the user repository for data access
     * @param passwordHasher the password hasher for password operations
     * @param captchaGenerator the CAPTCHA generator for challenge creation
     */
    public AuthenticationService(UserRepository userRepository,
                                 PasswordHasher passwordHasher,
                                 CaptchaGenerator captchaGenerator,
                                 SecurityQuestions securityQuestions,
                                 PasswordRecovery passwordRecovery,
                                 AuditLogger auditLogger,
                                 IpProvider ipProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.captchaGenerator = captchaGenerator;
        this.securityQuestions = securityQuestions;
        this.passwordRecovery = passwordRecovery;
        this.auditLogger = auditLogger;
        this.ipProvider = ipProvider;
    }

    /**
     * Registers a new user with the system.
     *
     * @param userId the unique user identifier
     * @param password the plain text password
     * @return true if registration successful, false otherwise
     * @throws UserAlreadyExistsException if user already exists
     * @throws PasswordHashException if password hashing fails
     * @throws IllegalArgumentException if inputs are invalid
     */
    public boolean registerUser(String userId, String password, String securityQuestion, String securityAnswer)
            throws UserAlreadyExistsException, PasswordHashException {
        validateUserId(userId);
        validatePassword(password);
        validateSecurityQuestion(securityQuestion);
        validateSecurityAnswer(securityAnswer);

        String hashedPassword = passwordHasher.hashPassword(password);
        String hashedAnswer = passwordHasher.hashPassword(securityAnswer);
        User user = new User(userId, hashedPassword, securityQuestion, hashedAnswer);
        if (userRepository.getUserCount() == 0) {
            user.setAdmin(true);
        }
        userRepository.addUser(user);
        return true;
    }

    /**
     * Registers a new user with the system.
     *
     * @param userId the unique user identifier
     * @param password the plain text password
     * @return true if registration successful, false otherwise
     * @throws UserAlreadyExistsException if user already exists
     * @throws PasswordHashException if password hashing fails
     * @throws IllegalArgumentException if inputs are invalid
     */
    public AuthenticationResult handlePasswordRecovery(String token, String userId, String newPassword)
            throws PasswordHashException {
        if (newPassword == null || newPassword.isEmpty())
            return new AuthenticationResult(false, "Password cannot be empty");

        if (!passwordRecovery.resetPassword(token, userId, newPassword))
            return new AuthenticationResult(false, "Password Reset Failed");

        return new AuthenticationResult(true, "Password Reset Successfull");
    }

    /**
     * Authenticates a user with two-factor authentication.
     * This method performs both password verification and CAPTCHA validation.
     *
     * @param userId the user identifier
     * @param password the plain text password
     * @param captcha the CAPTCHA challenge
     * @param captchaResponse the user's CAPTCHA response
     * @return an AuthenticationResult indicating success or failure
     * @throws PasswordHashException if password verification fails
     */
    public AuthenticationResult authenticate(String userId, String password,
                                             Captcha captcha, String captchaResponse)
            throws PasswordHashException {
        // Validate inputs
        if (userId == null || userId.trim().isEmpty()) {
            log("", false, "LOGIN");
            return new AuthenticationResult(false, "User ID cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            log(userId, false, "LOGIN");
            return new AuthenticationResult(false, "Password cannot be empty");
        }

        // Check if user exists
        User user = userRepository.getUserById(userId);
        if (user == null) {
            log(userId, false, "LOGIN");
            return new AuthenticationResult(false, "Invalid user ID or password");
        }

        // Verify password
        boolean passwordValid = passwordHasher.verifyPassword(password, user.getHashedPassword());
        if (!passwordValid) {
            log(userId, false, "LOGIN");
            return new AuthenticationResult(false, "Invalid user ID or password");
        }

        // Validate CAPTCHA
        boolean captchaValid = captchaGenerator.validateCaptcha(captcha, captchaResponse);
        if (!captchaValid) {
            log(userId, false, "LOGIN");
            return new AuthenticationResult(false, "Invalid CAPTCHA response");
        }

        long previous = user.getLastSuccessfulLoginAt();
        user.setLastSuccessfulLoginAt(System.currentTimeMillis());
        userRepository.updateUser(user);
        log(userId, true, "LOGIN");
        return new AuthenticationResult(true, "Authentication successful", previous);
    }

    private void log(String userId, boolean success, String event) {
        if (auditLogger == null || ipProvider == null) return;
        auditLogger.log(new AuditRecord(System.currentTimeMillis(), userId, ipProvider.getIp(), success, event));
    }

    /**
     * Registers a new user with the system.
     *
     * @param userId the unique user identifier
     * @param password the plain text password
     * @return true if registration successful, false otherwise
     * @throws UserAlreadyExistsException if user already exists
     * @throws PasswordHashException if password hashing fails
     * @throws IllegalArgumentException if inputs are invalid
     */
    public String initiatePasswordRecovery(String userId, String securityAnswer)
            throws UserNotFoundException, IllegalArgumentException, PasswordHashException {
        return passwordRecovery.initiateRecovery(userId, securityAnswer);
    }

    /**
     * Generates a new CAPTCHA challenge.
     *
     * @return a new Captcha object
     */
    public Captcha generateCaptcha() {
        return captchaGenerator.generateCaptcha();
    }

    /**
     * Generates Security Questions.
     *
     * @return Security Questions
     */
    public String[] generateSecurityQuestions() {
        return securityQuestions.getAllQuestions();
    }

    /**
     * Checks if a user exists in the system.
     *
     * @param userId the user ID to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String userId) {
        return userRepository.userExists(userId);
    }

    /**
     * Checks if a user exists in the system.
     *
     * @param userId the user ID to check
     * @return true if user exists, false otherwise
     */
    public User getUser(String userId) {
        return userRepository.getUserById(userId);
    }

    /**
     * Validates the user ID format and requirements.
     *
     * @param userId the user ID to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (userId.length() < 3) {
            throw new IllegalArgumentException("User ID must be at least 3 characters long");
        }
        if (userId.length() > 50) {
            throw new IllegalArgumentException("User ID cannot exceed 50 characters");
        }
        if (!userId.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("User ID can only contain letters, numbers, and underscores");
        }
    }

    /**
     * Validates the password format and requirements.
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException("Password cannot exceed 100 characters");
        }
    }

    /**
     * Validates the Security Answer
     *
     * @param securityAnswer the Security Answer to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSecurityAnswer(String securityAnswer) {
        if (securityAnswer == null || securityAnswer.isEmpty()) {
            throw new IllegalArgumentException("Security Answer cannot be null or empty");
        }
    }

    /**
     * Validates the Security Question
     *
     * @param securityQuestion the Security Question to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSecurityQuestion(String securityQuestion) {
        if (securityQuestion == null || securityQuestion.isEmpty()) {
            throw new IllegalArgumentException("Security Question cannot be null or empty");
        }
    }

    public int getTokenExpirationTime(){
        return passwordRecovery.getTokenExpirationTime();
    }
}