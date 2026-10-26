package com.TrialCompass.TrialCompass.service;

import com.TrialCompass.TrialCompass.DTO.ReportCriteria;
import com.TrialCompass.TrialCompass.DTO.ReportInfo;
import com.TrialCompass.TrialCompass.DTO.ReportResult;
import com.TrialCompass.TrialCompass.exception.InvalidRequestException;
import com.TrialCompass.TrialCompass.exception.ResourceNotFoundException;
import com.TrialCompass.TrialCompass.report.Report;
import com.TrialCompass.TrialCompass.repository.TrackingStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//report running logic

@Service
public class ReportService {

    private final Map<String, Report> reports = new LinkedHashMap<>();
    private final UserService userService;
    private final TrackingStatusRepository trackingStatusRepository;


    public ReportService(List <Report> allReports, UserService userService, TrackingStatusRepository trackingStatusRepository) {
        this.userService = userService;
        this.trackingStatusRepository = trackingStatusRepository;

        //every class that extends report gets passed in
        allReports.stream().sorted(Comparator.comparing(Report::getTitle)).forEach(r -> {
            //fails if two reports share the same type
            if(reports.put(r.getType(), r) != null) {
                throw new IllegalStateException("Two reports use the type " + r.getType());
            }
        });
    }

    //populates the reports picker with available reports
    public List<ReportInfo> listAvailable() {
        return reports.values().stream().map(r -> new ReportInfo(r.getType(), r.getTitle(), r.getDescription())).toList();
    }


    @Transactional(readOnly = true)
    public ReportResult run(String email, String type, ReportCriteria criteria) {
        Report report = reports.get(type);
        if(report == null) {
            throw new ResourceNotFoundException("Report not found");
        }

        //clean up data input w/helper method
        String keyword = clean(criteria.keyword());
        String status = clean(criteria.trackingStatus());

        if(criteria.from() != null && criteria.to() != null && criteria.from().isAfter(criteria.to())) {
            throw new InvalidRequestException("The start date must be on or before the end date");
        }

        if(status != null && trackingStatusRepository.findByCode(status).isEmpty()) {
            throw new InvalidRequestException("Unknown tracking status: " + status);
        }

        Long userId = userService.getByEmail(email).getId();

        return report.generate(userId, new ReportCriteria(keyword, status, criteria.from(), criteria.to()));
    }

    private String clean(String value) {
        if( value == null ) {
            return null;
        }
        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
