package com.TrialCompass.TrialCompass.report;


import com.TrialCompass.TrialCompass.DTO.ReportCriteria;
import com.TrialCompass.TrialCompass.DTO.ReportResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//parent for every report, serves as the blueprint class
//this class demonstrates inheritance, polymorphism and encapsulation to satisfy requirements
//outline in the capstone rubric
public abstract class Report {

    private final String type;  //url parameter
    private final String title;
    private final String description;


    protected Report(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    //template for all report results - this is protected and cannot be changed subclasses are used to provide the information
    public final ReportResult generate(Long userId, ReportCriteria criteria) {
        LocalDateTime generatedAt = LocalDateTime.now(); //time stamp when the report is generated

        List<List<String>>rows = buildRows(userId, criteria, generatedAt.toLocalDate());

        return new ReportResult(type, title, description, generatedAt, getColumns(), rows, rows.size());
    }

    //subclasses to be used for the report result method
    protected abstract List<String> getColumns(); //column headings

    protected abstract List<List<String>> buildRows(Long userId, ReportCriteria criteria, LocalDate today); //list of rows, each rows corresponds to a column


    //helpers===================

    //string ... allows 0 or more args
    //returns true if no keyword is given or if a field contains it
    protected static boolean matchesKeyword(String keyword, String... fields){
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String needle = keyword.trim().toLowerCase();
        for(String field : fields) {
            if(field != null && field.toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }

    //true if date falls within range of from/to
    protected static boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
        if(from != null && (date == null || date.isBefore(from))) {
            return false;
        }

        if(to != null && (date == null || date.isAfter(to))){
            return false;
        }
        return true;
    }

    //makes the not yet recruiting field more readable
    protected static String pretty(String code) {
        if(code == null || code.isBlank()) {
            return "Unknown";
        }
        String text = code.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    protected static String dateText(LocalDate date) {
        return date == null ? "" : date.toString();
    }


}
