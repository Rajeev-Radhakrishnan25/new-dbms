package com.lightweightDbms.auth;

/**
 * Interface for CAPTCHA generation and validation.
 * This interface follows the Interface Segregation Principle.
 *
 */
public interface CaptchaGenerator {
    /**
     * Generates a new CAPTCHA challenge.
     *
     * @return a Captcha object containing the challenge and answer
     */
    Captcha generateCaptcha();

    /**
     * Validates a CAPTCHA response.
     *
     * @param captcha the original captcha challenge
     * @param userResponse the user's response to validate
     * @return true if the response is correct, false otherwise
     */
    boolean validateCaptcha(Captcha captcha, String userResponse);
}