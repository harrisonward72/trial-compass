package com.TrialCompass.TrialCompass.service;


import com.TrialCompass.TrialCompass.DTO.RecruitmentStatusResponse;
import com.TrialCompass.TrialCompass.DTO.StudySearchRequest;
import com.TrialCompass.TrialCompass.DTO.StudySearchResponse;
import com.TrialCompass.TrialCompass.DTO.StudySummary;
import com.TrialCompass.TrialCompass.client.ClinicalTrialsClient;
import com.TrialCompass.TrialCompass.client.GeoPoint;
import com.TrialCompass.TrialCompass.client.ZipGeocodingService;
import com.TrialCompass.TrialCompass.entity.RecruitmentStatus;
import com.TrialCompass.TrialCompass.exception.InvalidSearchException;
import com.TrialCompass.TrialCompass.repository.RecruitmentStatusRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//takes the search request to the gov API call
//turns the API response to small DTOs
@Service
public class StudySearchService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_RADIUS_MILES = 1000;
    private static final int SUMMARY_MAX_CHARS = 300;

    private final ClinicalTrialsClient trialsClient;
    private final ZipGeocodingService geocodingService;
    private final RecruitmentStatusRepository statusRepository;


    public StudySearchService(ClinicalTrialsClient trialsClient, ZipGeocodingService geocodingService, RecruitmentStatusRepository statusRepository) {
        this.trialsClient = trialsClient;
        this.geocodingService = geocodingService;
        this.statusRepository = statusRepository;
    }

    //delivers the status checkboxes to the search page
    public List<RecruitmentStatusResponse> getStatuses() {
        return statusRepository.findAllByOrderBySortOrderAsc().stream().map(s -> new RecruitmentStatusResponse(s.getCode(), s.getLabel())).toList();
    }

    //returns a search response
    public StudySearchResponse search(StudySearchRequest request) {
        //clean the filters - trim and treat a blank filter as not used
        String condition = clean(request.condition());
        String keyword = clean(request.keyword());

        //location is only used if a zipcode is entered
        GeoPoint geo = null;
        int radius = request.radiusMiles() != null ? request.radiusMiles() : DEFAULT_RADIUS_MILES;
        if(request.zip() != null && request.zip().isBlank()) {
            geo = geocodingService.lookup(request.zip().trim());
        }

        // every status code needs to exist in the db - values placed in the url come from the db not the user
        List<String> statuses = validateStatuses(request.statuses());

        int pageSize = request.pageSize() != null ? request.pageSize() : DEFAULT_PAGE_SIZE;
        String pageToken = clean(request.pageToken());

        ClinicalTrialsClient.SearchCriteria criteria = new ClinicalTrialsClient.SearchCriteria(
                condition, keyword, geo, radius, request.sex(), request.age(), statuses, pageToken, pageSize
        );

        ClinicalTrialsClient.Response response = trialsClient.search(criteria);

        //maps each gov study to smaller dto objects.
        Map<String, String>labels = statusRepository.findAllByOrderBySortOrderAsc().stream().collect(Collectors.toMap(RecruitmentStatus::getCode, RecruitmentStatus::getLabel));

        List<StudySummary> studies = response.studies() == null ? List.of() : response.studies().stream().map(s -> toSummary(s, labels)).toList();

        long total = response.totalCount() != null ? response.totalCount() : studies.size();
        return new StudySearchResponse(studies, total, response.nextPageToken());
    }


    //helper methods =================================================================================

    private List<String> validateStatuses(List<String> requested) {
        if(requested == null || requested.isEmpty()) {
            return List.of(); // if there is no status filter return all statuses
        }

        //linked hash set removes duplicates and keeps things ordered
        List<String> codes = new ArrayList<>(new LinkedHashSet<>(requested));
        List<String> known = statusRepository.findByCodeIn(codes).stream().map(RecruitmentStatus::getCode).toList();
        if(known.size() != codes.size()) {
            throw new InvalidSearchException("A selected status is not valid");
        }
        return codes;
    }

    private StudySummary toSummary(ClinicalTrialsClient.Study study, Map<String, String> labels) {
        ClinicalTrialsClient.Protocol p = study.protocolSection();
        if(p == null) {
            return null;
        }

        var id = p.identificationModule();
        var status = p.statusModule();
        var eligibility = p.eligibilityModule();

        //checks for code conditions and locations returns if present returns whole list if not
        String code = status != null ? status.overallStatus() : null;

        List<String> conditions = (p.conditionsModule() != null && p.conditionsModule().conditions() != null) ? p.conditionsModule().conditions() : List.of();

        List<ClinicalTrialsClient.LocationItem> locations = (p.contactsLocationsModule() != null && p.contactsLocationsModule().locations() != null) ?  p.contactsLocationsModule().locations() : List.of();

        //allows for 3 different location labels
        List<String> places = locations.stream().map(this::placeLabel).filter(s -> !s.isBlank()).distinct().limit(3).toList();

        //returns a study summary object - checks for attributes and returns null if they don't exist
        return new StudySummary(
                id != null ? id.nctId() : null,
                id != null ? id.briefTitle() : null,
                code,
                code == null ? "Unknown" : labels.getOrDefault(code, code),
                shorten(p.descriptionModule() != null ? p.descriptionModule().briefSummary(): null),
                conditions,
                (id != null && id.organization() != null) ? id.organization().fullName() : null,
                eligibility != null ? eligibility.sex() : null,
                ageRange(eligibility),
                places,
                locations.size(),
                (status != null && status.lastUpdatePostDateStruct() != null) ? status.lastUpdatePostDateStruct().date() : null);
    }

    //places label for country or state
    private String placeLabel(ClinicalTrialsClient.LocationItem l) {
        String region = "United States".equals(l.country()) ? l.state() : l.country();
        return java.util.stream.Stream.of(l.city(), region).filter(s-> s != null && !s.isBlank()).collect(Collectors.joining(", "));
    }

    //determines if an age range was specified or not
    //returns values based on whether its specified
    private String ageRange(ClinicalTrialsClient.Eligibility e) {

        if(e == null) {
            return null;
        }

        boolean hasMin = e.minimumAge() != null;
        boolean hasMax = e.maximumAge() != null;

        if(hasMin && hasMax) {
            return e.minimumAge() + " to " + e.maximumAge();
        }

        if(hasMin) {
            return e.minimumAge() + " and older";
        }

        if(hasMax) {
            return e.maximumAge() + " and younger";
        }

        return "All ages";
    }

    //shortens the amount of text to be displayed from the summary
    private String shorten(String text) {
        if(text == null) {
            return null;
        }

        String flat = text.replaceAll("\\s+", " ").trim(); //trim whitespace
        return flat.length() <= SUMMARY_MAX_CHARS ? flat : flat.substring(0, SUMMARY_MAX_CHARS).trim();
    }

    private String clean(String value) {
        if(value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
