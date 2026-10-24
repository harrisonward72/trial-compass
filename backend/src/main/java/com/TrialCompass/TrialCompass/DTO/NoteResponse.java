package com.TrialCompass.TrialCompass.DTO;

import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
