package com.lightweightDbms.auth;

import java.util.Random;

/**
 * Implements a simple CAPTCHA generator with mathematical and pattern challenges.
 * This class generates random challenges including arithmetic operations and
 * simple pattern recognition.
 *
 */
public class SimpleCaptchaGenerator implements CaptchaGenerator {
    private final Random random;
    private static final int MAX_NUMBER = 20;

    /**
     * Constructs a new SimpleCaptchaGenerator.
     */
    public SimpleCaptchaGenerator() {
        this.random = new Random();
    }

    /**
     * Generates a new CAPTCHA challenge.
     * The challenge can be one of several types:
     * - Simple addition
     * - Simple subtraction
     * - Number sequence
     * - Pattern recognition
     *
     * @return a Captcha object containing the challenge and answer
     */
    @Override
    public Captcha generateCaptcha() {
        int type = random.nextInt(4);

        return switch (type) {
            case 0 -> generateAdditionCaptcha();
            case 1 -> generateSubtractionCaptcha();
            case 2 -> generateSequenceCaptcha();
            default -> generatePatternCaptcha();
        };
    }

    /**
     * Validates a CAPTCHA response.
     *
     * @param captcha the original captcha challenge
     * @param userResponse the user's response to validate
     * @return true if the response is correct, false otherwise
     */
    @Override
    public boolean validateCaptcha(Captcha captcha, String userResponse) {
        if (captcha == null || userResponse == null) {
            return false;
        }
        if (captcha.isExpired()) {
            return false;
        }
        return captcha.getAnswer().equalsIgnoreCase(userResponse.trim());
    }

    /**
     * Generates an addition CAPTCHA challenge.
     *
     * @return a Captcha with an addition problem
     */
    private Captcha generateAdditionCaptcha() {
        int a = random.nextInt(MAX_NUMBER) + 1;
        int b = random.nextInt(MAX_NUMBER) + 1;
        String challenge = String.format("What is %d + %d?", a, b);
        String answer = String.valueOf(a + b);
        return new Captcha(challenge, answer);
    }

    /**
     * Generates a subtraction CAPTCHA challenge.
     *
     * @return a Captcha with a subtraction problem
     */
    private Captcha generateSubtractionCaptcha() {
        int a = random.nextInt(MAX_NUMBER) + 10;
        int b = random.nextInt(10) + 1;
        String challenge = String.format("What is %d - %d?", a, b);
        String answer = String.valueOf(a - b);
        return new Captcha(challenge, answer);
    }

    /**
     * Generates a number sequence CAPTCHA challenge.
     *
     * @return a Captcha with a sequence completion problem
     */
    private Captcha generateSequenceCaptcha() {
        int start = random.nextInt(10) + 1;
        int step = random.nextInt(3) + 2;
        String challenge = String.format("Complete the sequence: %d, %d, %d, ?",
                start, start + step, start + 2 * step);
        String answer = String.valueOf(start + 3 * step);
        return new Captcha(challenge, answer);
    }

    /**
     * Generates a pattern CAPTCHA challenge.
     *
     * @return a Captcha with a pattern recognition problem
     */
    private Captcha generatePatternCaptcha() {
        int num = random.nextInt(5) + 3;
        String pattern = String.valueOf(num).repeat(3);
        String challenge = String.format("What number is repeated in this pattern: %s", pattern);
        String answer = String.valueOf(num);
        return new Captcha(challenge, answer);
    }
}