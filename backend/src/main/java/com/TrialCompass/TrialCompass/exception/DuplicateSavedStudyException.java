package com.TrialCompass.TrialCompass.exception;

public class DuplicateSavedStudyException extends RuntimeException {
    public DuplicateSavedStudyException(String nctId) {
        super("This trial has already been saved" + nctId);
    }
}
