package com.TrialCompass.TrialCompass.DTO;

//this gets sent back after registration - password left out to keep it protected
public record UserResponse(
        Long id,
        String email,
        String firstName
       // String lastName
) {
}
