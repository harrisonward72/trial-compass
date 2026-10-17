package com.TrialCompass.TrialCompass.controller;


import com.TrialCompass.TrialCompass.DTO.RecruitmentStatusResponse;
import com.TrialCompass.TrialCompass.DTO.StudySearchRequest;
import com.TrialCompass.TrialCompass.DTO.StudySearchResponse;
import com.TrialCompass.TrialCompass.service.StudySearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/studies")
public class StudyController {

    private final StudySearchService searchService;

    public StudyController(StudySearchService searchService) {
        this.searchService = searchService;
    }

    //get query params - @ModelAttribute fills the record from the query string
    @GetMapping("/search")
    public StudySearchResponse search(@Valid @ModelAttribute StudySearchRequest request) {
        return searchService.search(request);
    }

    //checkbox options
    @GetMapping("/statuses")
    public List<RecruitmentStatusResponse> statuses(){
        return searchService.getStatuses();
    }
}
