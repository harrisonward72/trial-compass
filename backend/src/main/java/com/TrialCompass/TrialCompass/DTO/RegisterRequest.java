package com.TrialCompass.TrialCompass.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//builds security into the application
//json body the client sends to register an account
public record RegisterRequest(

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255, message = "Email must not be longer than 255 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 70, message = "Password must be between 8 and 70 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
    String password,

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be less than 100 characters")
    String firstName

//    @NotBlank(message = "Last name is required")
//    @Size(max = 100, message = "Last name must be less than 100 characters")
//    String lastName
    ){}
