package com.TrialCompass.TrialCompass.DTO;

import java.time.LocalDateTime;
import java.util.List;

//a finished report - all the fields are what will populate within a report
public record ReportResult(

        String type,
        String title,
        String description,
        LocalDateTime generatedAt,
        List<String> columns,
        List<List<String>> rows,
        int rowCount
) {
}
