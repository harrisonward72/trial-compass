package com.TrialCompass.TrialCompass.DTO;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// body of PUT /api/saved-studies/{id}
public record UpdateSavedStudyRequest(

        @NotBlank(message = "Tracking status is required")
        @Size(max = 50, message = "Tracking status is too long")
        String trackingStatusCode,

        LocalDate followUpDate
) {
}
