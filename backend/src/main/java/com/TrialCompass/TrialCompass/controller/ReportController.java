package com.TrialCompass.TrialCompass.controller;

import com.TrialCompass.TrialCompass.DTO.ReportCriteria;
import com.TrialCompass.TrialCompass.DTO.ReportInfo;
import com.TrialCompass.TrialCompass.DTO.ReportResult;
import com.TrialCompass.TrialCompass.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//login required to access this path
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    //lists available reports
    @GetMapping
    public List<ReportInfo> list(){
        return reportService.listAvailable();
    }

    //filters reports by type using parameters from the url - date, title, status etc.
    @GetMapping("/{type}")
    public ReportResult run(@PathVariable String type, @Valid @ModelAttribute ReportCriteria criteria, Authentication authentication){
        return reportService.run(authentication.getName(), type, criteria);
    }


}
