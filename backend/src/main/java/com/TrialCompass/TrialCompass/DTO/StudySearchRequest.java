package com.TrialCompass.TrialCompass.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

//filter fields are optional
//use wrapper types so they can be null, primitive type cannot be null
//filter will ue the urls query string
public record StudySearchRequest(
        @Size(max = 200, message = "Condition must be 200 characters or less")
        String condition,
        @Size(max = 200, message = "Keywords must be 200 characters or less")
        String keyword,
        @Pattern(regexp = "^\\d{5}$", message = "Enter a 5-digit zip code")
        String zip,
        //not going to set distance parameters on radius
        Integer radiusMiles,
        SexFilter sex,
        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 130, message = "Age is too old, try again")
        Integer age,
        @Size(max = 20, message = "Too many status' selected")
        List<String>statuses,
        @Size(max = 200)
        String pageToken,
        @Min(1) @Max(50)
        Integer pageSize
) {
}
