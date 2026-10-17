package com.TrialCompass.TrialCompass.client;

import com.TrialCompass.TrialCompass.DTO.SexFilter;
import com.TrialCompass.TrialCompass.exception.ExternalServiceException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.tomcat.util.net.openssl.ciphers.Protocol;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;

//this class talks to clinicaltrials.gov builds the request from the provided filters and returns the response
//StudySearch service maps it to the DTOs
@Component
public class ClinicalTrialsClient {

    private static final Logger log = LoggerFactory.getLogger(ClinicalTrialsClient.class);

    //this list filters the api results so it doesn't return every field for every study
    private static final String FIELDS = String.join(",", "NCTId",
            "BriefTitle", "OverallStatus", "BriefSummary", "Condition", "OrgFullName",
            "Sex", "MinimumAge", "MaximumAge", "LocationFacility", "LocationCity", "LocationState",
            "LocationCountry", "LastUpdatePostDate");

    //search fields for the client
    public record SearchCriteria(
            String condition,
            String keyword,
            GeoPoint geo,
            int radiusMiles,
            SexFilter sex,
            Integer age,
            List<String> statuses,
            String pageToken,
            int pageSize
    ){}

    //shapes of the json - mirror the api
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Study> studies, Long totalCount, String nextPageToken){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Study(Protocol protocolSection) {}

    @JsonIgnoreProperties
    public record Protocol(
            Identification identificationModule,
            StatusModule statusModule,
            Description descriptionModule,
            Conditions conditionsModule,
            Eligibility eligibilityModule,
            ContactsLocations contactsLocationsModule
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Identification(String nctId, String briefTitle, Organization organization){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Organization(String fullName){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusModule(String overallStatus, DateStruct lastUpdatePostDateStruct){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateStruct(String date){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Description(String briefSummary){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Conditions(List<String> conditions){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Eligibility(String sex, String minimumAge, String maximumAge){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactsLocations(List<LocationItem> locations){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LocationItem(String facility, String city, String state, String country){}

    private final RestClient restClient;

    public ClinicalTrialsClient(@Value("${app.clinicaltrials.base-url}") String baseUrl) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).defaultHeader("User-Agent", "TrialCompass-Capstone/1.0").build();
    }


    public Response search(SearchCriteria c) {
        //builds url with placeholders and gives spring a map of values
        StringBuilder url = new StringBuilder(
                "/studies?countTotal=true&pageSize={pageSize}&fields={fields}");
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("pageSize", c.pageSize);
        vars.put("fields", FIELDS);

        if(c.condition() != null) {
            url.append("&query.cond={cond}");
            vars.put("cond", c.condition());
        }
        if(c.keyword != null) {
            url.append("&query.term={term}");
            vars.put("term", c.keyword());
        }
        if(c.statuses() != null && !c.statuses().isEmpty()) {
            url.append("&filter.overallStatus={statuses}");
            vars.put("statuses", String.join(",", c.statuses()));
        }
        if(c.geo() != null) {
            url.append("&filter.geo={geo}");
            vars.put("geo", "distance(" + c.geo().latitude() + "," + c.geo().longitude() + "," + c.radiusMiles() + "mi)");
        }

        //sex and age use the APIs advanced language - clauses are joined with AND
        //this input is either enum or int - not free text
        List<String> clauses = new ArrayList<>();
        if(c.sex() != null) {
            url.append("&aggFilters={sexFilter}");
            vars.put("sexFilter", "sex:" + (c.sex() == SexFilter.MALE ? "m" : "f"));
        }
        if(!clauses.isEmpty()) {
            url.append("&filter.advanced={advanced}");
            vars.put("advanced", String.join(" AND ", clauses));
        }
        if (c.age() != null) {
            clauses.add("(AREA[MinimumAge]RANGE[MIN, " + c.age() + " years] OR AREA[MinimumAge]MISSING)");
            clauses.add("(AREA[MaximumAge]RANGE[" + c.age() + " years, MAX] OR AREA[MaximumAge]MISSING)");
        }
        if(c.pageToken() != null) {
            url.append("&pageToken={pageToken}");
            vars.put("pageToken", c.pageToken());
        }

        try {
            return restClient.get().uri(url.toString(), vars).retrieve().body(Response.class);
        } catch (RestClientException e) {
            log.error("ClinicalTrials.gov request failed: {}", e.getMessage());
            System.out.println(e.getMessage());
            throw new ExternalServiceException("The clinical trials database is temporarily unavailable, try again soon", e);
        }

    }

}
