package com.TrialCompass.TrialCompass.report;

//every saved trial generated in this report class extends the parent class

import com.TrialCompass.TrialCompass.DTO.ReportCriteria;
import com.TrialCompass.TrialCompass.entity.SavedStudies;
import com.TrialCompass.TrialCompass.repository.NoteRepository;
import com.TrialCompass.TrialCompass.repository.SavedStudyRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SavedTrialsReport extends Report{

    private final SavedStudyRepository savedStudyRepository;
    private final NoteRepository noteRepository;


    public SavedTrialsReport(SavedStudyRepository savedStudyRepository, NoteRepository noteRepository) {
        super("saved-trials", "My Saved Trials", "Every trial you saved, with your progress, follow-up date, and number of notes" + "Date filters reflect the day you saved trials");

        this.savedStudyRepository = savedStudyRepository;
        this.noteRepository = noteRepository;

    }

    @Override
    protected List<String> getColumns() {
        return List.of("NCT ID", "Title", "Trial status", "My progress", "Follow-up date", "Notes", "Saved on");
    }



    @Override
    protected List<List<String>> buildRows(Long userId, ReportCriteria criteria, LocalDate today) {
        //SQL query counts the notes for all the users trials
        //if a trial doesn't have notes associated with it, it doesn't get counted
        Map<Long, Long> noteCounts = noteRepository.countNotesByStudyForUser(userId).stream().collect(Collectors.toMap(NoteRepository.NoteCount::getSavedStudyId, NoteRepository.NoteCount::getTotal));

        List<List<String>> rows = new ArrayList<>();

        //filter the saved trials
        for(SavedStudies s : savedStudyRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            LocalDate savedOn = s.getCreatedAt().toLocalDate();

            if(!inRange(savedOn, criteria.from(), criteria.to())) {
                continue;
            }
            if(criteria.trackingStatus() != null && !criteria.trackingStatus().equals(s.getTrackingStatus().getCode())) {
                continue;
            }
            rows.add(List.of(
                    s.getNctId(),
                    s.getTitle(),
                    pretty(s.getLastKnownStatus()),
                    s.getTrackingStatus().getLabel(),
                    dateText(s.getFollowUpDate()),
                    String.valueOf(noteCounts.getOrDefault(s.getId(), 0L)),
                    dateText(savedOn)));
        }
        return rows;


    }
}
