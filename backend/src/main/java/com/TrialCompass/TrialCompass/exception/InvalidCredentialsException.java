package com.TrialCompass.TrialCompass.exception;

//gets thrown when an email or password is wrong - doesnt specify which is wrong for security reasons
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
