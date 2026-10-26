package com.TrialCompass.TrialCompass.report;

import com.TrialCompass.TrialCompass.DTO.ReportCriteria;
import com.TrialCompass.TrialCompass.entity.SavedStudies;
import com.TrialCompass.TrialCompass.repository.SavedStudyRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//trials that have a follow up date, the ones with the soonest date are listed first

@Component
public class FollowUpReport extends Report{

    private final SavedStudyRepository savedStudyRepository;

    public FollowUpReport(SavedStudyRepository savedStudyRepository) {
        super("follow-up trials", "Upcoming follow ups", "Trials with a follow up date, ordered by soonest first");
        this.savedStudyRepository = savedStudyRepository;

    }

    @Override
    protected List<String> getColumns() {
        return List.of("Follow-up date", "When", "NCT-ID", "Title", "My Progress", "Trial Status");
    }

    @Override
    protected List<List<String>> buildRows(Long userId, ReportCriteria criteria, LocalDate today) {
        List<SavedStudies> matches = new ArrayList<>();

        //criteria for saved study to have to be added to the generated report
        for(SavedStudies s: savedStudyRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            if (s.getFollowUpDate() == null) {
                continue;
            }

            if (!inRange(s.getFollowUpDate(), criteria.from(), criteria.to())) {
                continue;
            }

            if (criteria.trackingStatus() != null && !criteria.trackingStatus().equals(s.getTrackingStatus().getCode())) {
                continue;
            }

            if (!matchesKeyword(criteria.keyword(), s.getTitle(), s.getNctId())) {
                continue;
            }

            matches.add(s);
        }

            matches.sort(Comparator.comparing(SavedStudies :: getFollowUpDate));

            List<List<String>> rows = new ArrayList<>();

            for(SavedStudies s : matches) {
                rows.add(List.of(
                        dateText(s.getFollowUpDate()),
                        whenText(today, s.getFollowUpDate()),
                        s.getNctId(),
                        s.getTitle(),
                        s.getTrackingStatus().getLabel(),
                        pretty(s.getLastKnownStatus())
                ));
            }
            return rows;
        }

    //over due by how many days - or due in how many days
    private String whenText(LocalDate today, LocalDate followUp) {
        long days = ChronoUnit.DAYS.between(today, followUp);
        if(days < 0) {
            return "Overdue by " + (-days) + (days == -1 ? " day" : " days");
        }
        if(days == 0) {
            return "Today";
        }
        return "In " + days + (days == 1 ? " day" : " days");
    }
}
