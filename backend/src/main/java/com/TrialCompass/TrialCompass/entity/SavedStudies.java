package com.TrialCompass.TrialCompass.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

//serves as an object for saved trials

@Entity
@Table(name = "saved_studies")
public class SavedStudies extends BaseEntity{

    //Many-to-one: many saved trials can belong to one user
    //lazy means not to load the whole user row unless its asked for - updatable=false means a row cannot be moved to another user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    //eager - status label is needed everytime a saved trial is shown
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tracking_status_id", nullable = false)
    private TrackingStatus trackingStatus;

    @Column(name = "nct_id", nullable = false, length = 20, updatable = false)
    private String nctId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "last_known_status", length = 50)
    private String lastKnownStatus;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    protected SavedStudies(){

    }

    public SavedStudies(User user, TrackingStatus trackingStatus, String nctId, String title, String lastKnownStatus, LocalDate followUpDate) {
        this.user = user;
        this.trackingStatus = trackingStatus;
        this.nctId = nctId;
        this.title = title;
        this.lastKnownStatus = lastKnownStatus;
        this.followUpDate = followUpDate;
    }

    public User getUser() {
        return user;
    }

    public TrackingStatus getTrackingStatus() {
        return trackingStatus;
    }

    public String getNctId() {
        return nctId;
    }

    public String getTitle() {
        return title;
    }

    public String getLastKnownStatus() {
        return lastKnownStatus;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    //only the follow up date and tracking status are changeable

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }

    public void setTrackingStatus(TrackingStatus trackingStatus) {
        this.trackingStatus = trackingStatus;
    }
}
