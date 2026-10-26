package com.TrialCompass.TrialCompass.DTO;

import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReportCriteria (

        @Size(max=100, message = "Keyword must be less than 100 characters")
        String keyword,

        @Size(max = 50, message = "tracking status must be less than 50 characters")
        String trackingStatus,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
){
}
