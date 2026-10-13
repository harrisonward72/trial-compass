package com.TrialCompass.TrialCompass.exception;


//custom exception to get thrown if an email already exists
public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException(String email) {
        super("An account with the email, " + email + " already exists");
    }
}
