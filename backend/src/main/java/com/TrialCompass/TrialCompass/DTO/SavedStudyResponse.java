package com.TrialCompass.TrialCompass.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

// body of how a saved trial will appear on the dashboard
public record SavedStudyResponse(

        Long id,
        String nctId,
        String title,
        String lastKnownStatus,
        String trackingStatusCode,
        String trackingStatusLabel,
        LocalDate followUpDate,
        LocalDateTime savedAt

) {
}
