package com.lightweightDbms.auth;

public class SimpleSecurityQuestions implements SecurityQuestions{
    final String[] securityQuestions = {
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What was the name of your first school?",
            "In what city were you born?",
            "What is your favorite food?",
            "What is your favorite color?",
            "What is the name of your best childhood friend?",
            "What is your father's middle name?",
            "What was your childhood nickname?",
            "What is the name of your favorite teacher?",
            "What was your first car?",
            "What was the name of the street you grew up on?",
            "What is your favorite movie?",
            "What is your favorite book?",
            "What was the name of your first employer?"
    };

    public String[] getAllQuestions() {
        return securityQuestions;
    }
}