package com.TrialCompass.TrialCompass.DTO;

import java.util.List;

//one search result row, keep only the results that match the filters
public record StudySummary(
        String nctId,
        String title,
        String status,
        String statusLabel,
        String summary,
        List<String>conditions,
        String organization,
        String sex,
        String ageRange,
        List<String>locations,
        int totalLocations,
        String lastUpdated
) {
}
