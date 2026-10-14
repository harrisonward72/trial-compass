package com.TrialCompass.TrialCompass.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//json body for log in
//checks that fields are present
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
