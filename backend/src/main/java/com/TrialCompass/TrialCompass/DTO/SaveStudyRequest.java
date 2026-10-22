package com.TrialCompass.TrialCompass.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

//body of POST /api/saved-studies - server uses login token for verification not the user ID
public record SaveStudyRequest(
        @NotBlank(message = "Trial ID is required")
        //nct ids start with NCT followed by 8 digits
        @Pattern(regexp = "^NCT\\d{8}$", message = "Trial ID must resemble 'NCT12345678'")
        String nctId,

        @NotBlank(message = "Title is required")
        @Size(max = 500, message = "Title must be 500 characters or less")
        String title,

        @Size(max = 50, message = "Status is too long")
        String lastKnownStatus,

        @Size(max = 50, message = "Tracking status is too long")
        String trackingStatusCode,

        LocalDate followUpDate
) {
}
