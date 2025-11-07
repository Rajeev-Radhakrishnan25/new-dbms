package com.lightweightDbms.auth;

/**
 * Represents a CAPTCHA challenge with its question and answer.
 *
 */
public class Captcha {
    private final String challenge;
    private final String answer;
    private final long createdAt;
    private static final long VALIDITY_DURATION = 300000; // 5 minutes

    /**
     * Constructs a new Captcha with the specified challenge and answer.
     *
     * @param challenge the CAPTCHA challenge question
     * @param answer the correct answer to the challenge
     */
    public Captcha(String challenge, String answer) {
        this.challenge = challenge;
        this.answer = answer;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Gets the CAPTCHA challenge question.
     *
     * @return the challenge question
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Gets the correct answer to the CAPTCHA.
     *
     * @return the correct answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Checks if the CAPTCHA has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > VALIDITY_DURATION;
    }
}
