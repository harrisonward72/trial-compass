package com.TrialCompass.TrialCompass.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
        @NotBlank(message = "A note cannot be empty")
        @Size(max = 2000, message = "Notes must be less than 2000 characters")
        String body
) {
}
