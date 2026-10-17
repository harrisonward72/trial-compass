package com.TrialCompass.TrialCompass.DTO;

import java.util.List;

public record StudySearchResponse(
        List<StudySummary> studies,
        long totalCount,
        String nextPageToken
) {
}
